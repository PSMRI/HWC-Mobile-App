package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.ImmunizationDao
import org.piramalswasthya.cho.model.ImmunizationCache
import org.piramalswasthya.cho.model.ImmunizationPost
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ImmunizationRepo @Inject constructor(
    private val immunizationDao: ImmunizationDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService,
    private val patientRepo: PatientRepo,
) {

//    suspend fun getImmunizationDetailsFromServer(): Int {
//        return withContext(Dispatchers.IO) {
//            val user =
//                preferenceDao.getLoggedInUser()
//                    ?: throw IllegalStateException("No user logged in!!")
//            val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
//            try {
//                val response = tmcNetworkApiService.getChildImmunizationDetails(
//                    GetDataPaginatedRequest(
//                        ashaId = user.userId,
//                        pageNo = 0,
//                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
//                        toDate = getCurrentDate()
//                    )
//                )
//                val statusCode = response.code()
//                if (statusCode == 200) {
//                    val responseString = response.body()?.string()
//                    if (responseString != null) {
//                        val jsonObj = JSONObject(responseString)
//
//                        val errorMessage = jsonObj.getString("errorMessage")
//                        val responseStatusCode = jsonObj.getInt("statusCode")
//                        Timber.d("Pull from amrit child immunization data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    val dataObj = jsonObj.getString("data")
//                                    saveImmunizationCacheFromResponse(dataObj)
//                                } catch (e: Exception) {
//                                    Timber.d("Child Immunization entries not synced $e")
//                                    return@withContext 0
//                                }
//
//                                return@withContext 1
//                            }
//
//                            5002 -> {
//                                if (userRepo.refreshTokenTmc(
//                                        user.userName, user.password
//                                    )
//                                ) throw SocketTimeoutException("Refreshed Token!")
//                                else throw IllegalStateException("User Logged out!!")
//                            }
//
//                            5000 -> {
//                                if (errorMessage == "No record found") return@withContext 0
//                            }
//
//                            else -> {
//                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
//                            }
//                        }
//                    }
//                }
//
//            } catch (e: SocketTimeoutException) {
//                Timber.d("get_child_immunization error : $e")
//                return@withContext -2
//
//            } catch (e: java.lang.IllegalStateException) {
//                Timber.d("get_child_immunization error : $e")
//                return@withContext -1
//            }
//            -1
//        }
//    }
//
//    private suspend fun saveImmunizationCacheFromResponse(dataObj: String): List<ImmunizationPost> {
//        val immunizationList = Gson().fromJson(dataObj, Array<ImmunizationPost>::class.java).toList()
//        immunizationList.forEach { immunizationDTO ->
//            val vaccine = immunizationDao.getVaccineByName(immunizationDTO.vaccineName)!!
//            var immunizationCache: ImmunizationCache? =
//                immunizationDao.getImmunizationRecord(immunizationDTO.beneficiaryId, vaccine.id)
//            if (immunizationCache == null) {
//                var immunizationCache = immunizationDTO.toCacheModel()
//                val vaccine = immunizationDao.getVaccineByName(immunizationDTO.vaccineName)!!
//                immunizationCache.vaccineId = vaccine.id
//                immunizationDao.addImmunizationRecord(immunizationDTO.toCacheModel())
//            }
//        }
//        return immunizationList
//    }

    suspend fun pushUnSyncedChildImmunizationRecords(): Boolean {

        return withContext(Dispatchers.IO) {
            val user =
                userRepo.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val immunizationCacheList: List<ImmunizationCache> = immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED)

            val immunizationDTOs = mutableListOf<ImmunizationPost>()
            immunizationCacheList.forEach { cache ->
                val patient = patientRepo.getPatient(cache.patID)
                var immunizationDTO = cache.asPostModel(patient.beneficiaryID!!)
                val vaccine = immunizationDao.getVaccineById(cache.vaccineId)!!
                immunizationDTO.vaccineName = vaccine.vaccineName
                immunizationDTOs.add(immunizationDTO)
            }
            if (immunizationDTOs.isEmpty()) return@withContext true
            try {
                val response = tmcNetworkApiService.postChildImmunizationDetails(immunizationDTOs)
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to Amrit Child Immunization data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    updateSyncStatusImmunization(immunizationCacheList)
                                    return@withContext true
                                } catch (e: Exception) {
                                    Timber.d("Child Immunization entries not synced $e")
                                }
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                Log.d("child immunization fails", errorMessage)
                                if (errorMessage == "No record found") return@withContext false
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("save_child_immunization error : $e")
                return@withContext false

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("save_child_immunization error : $e")
                return@withContext false
            }
            false
        }
    }

    private suspend fun updateSyncStatusImmunization(immunizationList: List<ImmunizationCache>) {
        immunizationList.forEach {
            it.syncState = SyncState.SYNCED
            it.processed = "P"
            immunizationDao.addImmunizationRecord(it)
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        private fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
            val dateString = dateFormat.format(millis)
            val timeString = timeFormat.format(millis)
            return "${dateString}T${timeString}.000Z"
        }

        private fun getLongFromDate(dateString: String): Long {
            //Jul 22, 2023 8:17:23 AM"
            val f = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
        }
    }
}