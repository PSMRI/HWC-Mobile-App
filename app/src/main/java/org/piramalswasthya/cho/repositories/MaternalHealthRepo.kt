package org.piramalswasthya.cho.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
//import org.piramalswasthya.cho.database.room.InAppDb
//import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.AbortionDomain
import org.piramalswasthya.cho.model.PatientWithPwrCache
import org.piramalswasthya.cho.model.PmsmaDomain
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
//import org.piramalswasthya.sakhi.database.room.dao.BenDao
//import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.model.ANCPost
//import org.piramalswasthya.sakhi.helpers.getTodayMillis
//import org.piramalswasthya.sakhi.model.*
//import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
//import org.piramalswasthya.sakhi.network.getLongFromDate
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class MaternalHealthRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val maternalHealthDao: MaternalHealthDao,
//    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao,
//    private val preferenceDao: PreferenceDao,
) {

    suspend fun getSavedRegistrationRecord(benId: String): PregnantWomanRegistrationCache? {
        return maternalHealthDao.getSavedRecord(benId)
    }


    suspend fun getLastVisitNumber(benId: String): Int? {
        return maternalHealthDao.getLastVisitNumber(benId)
    }

    suspend fun getSavedAncRecord(benId: String, visitNumber: Int): PregnantWomanAncCache? {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getSavedRecord(benId, visitNumber)
        }
    }


//
//    suspend fun getLatestAncRecord(benId: Long): PregnantWomanAncCache? {
//        return withContext(Dispatchers.IO) {
//            maternalHealthDao.getLatestAnc(benId)
//        }
//    }
//
    suspend fun getAllActiveAncRecords(benId: String): List<PregnantWomanAncCache> {
         return maternalHealthDao.getAllActiveAncRecords(benId)
    }

    suspend fun getLastAnc(benId: String): PregnantWomanAncCache? {
        return maternalHealthDao.getLastAnc(benId)
    }

    suspend fun persistRegisterRecord(pregnancyRegistrationForm: PregnantWomanRegistrationCache) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.saveRecord(pregnancyRegistrationForm)
        }
    }



    suspend fun persistAncRecord(ancCache: PregnantWomanAncCache) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.saveRecord(ancCache)
        }
    }

    suspend fun updateAncRecord(ancCache: Array<PregnantWomanAncCache>) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.updateANC(*ancCache)
        }
    }


    suspend fun processNewAncVisit(): Boolean {
        return withContext(Dispatchers.IO) {
            val ancList = maternalHealthDao.getAllUnprocessedAncVisits()

            val ancPostList = mutableSetOf<ANCPost>()
            ancList.forEach {
                ancPostList.clear()
                val ben = patientDao.getPatient(it.patientID)
                if(ben.beneficiaryID != null){
                    ancPostList.add(it.asPostModel(ben.beneficiaryID!!))
                    it.syncState = SyncState.SYNCING
                    maternalHealthDao.updateANC(it)
                    val uploadDone = postDataToAmritServer(ancPostList)
                    if (uploadDone) {
                        it.processed = "P"
                        it.syncState = SyncState.SYNCED
                    } else {
                        it.syncState = SyncState.UNSYNCED
                    }
                    maternalHealthDao.updateANC(it)
//                if (!uploadDone)
//                    return@withContext false
                }
            }

            return@withContext true
        }
    }

    /**
     * Get all patients with their pregnancy registration data
     */
    fun getAllPatientsWithPWR(): Flow<List<PatientWithPwrCache>> {
        return maternalHealthDao.getAllPatientsWithPWR()
    }

    /**
     * Get specific patient with pregnancy registration
     */
    suspend fun getPatientWithPWR(patientID: String): PatientWithPwrCache? {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getPatientWithPWR(patientID)
        }
    }

    /**
     * Get count of pregnant women registrations
     */
    fun getPWRCount(): Flow<Int> {
        return maternalHealthDao.getPWRCount()
    }

    /**
     * Get all patients who have delivered
     * Uses batch query to avoid N+1 queries
     */
    fun getAllDeliveredWomen(): Flow<List<PatientWithPwrCache>> {
        return maternalHealthDao.getDeliveredWomenPatientIDs()
            .transformLatest { patientIDs ->
                if (patientIDs.isNotEmpty()) {
                    val patients = maternalHealthDao.getDeliveredWomenByIDs(patientIDs)
                    emit(patients)
                } else {
                    emit(emptyList())
                }
            }
    }

    /**
     * Get count of delivered women
     */
    fun getDeliveredWomenCount(): Flow<Int> {
        return maternalHealthDao.getDeliveredWomenCount()
    }

    /**
     * Get all women with abortion records
     */
    fun getAbortionPregnantWomanList(): Flow<List<AbortionDomain>> {
        return maternalHealthDao.getAllAbortionWomenList()
            .map { list -> 
                list.map { it.asAbortionDomainModel() }
                    .filter { it.abortionDate != null } // Ensure abortion date exists
            }
    }

    /**
     * Get count of abortion women
     */
    fun getAbortionWomenCount(): Flow<Int> {
        return maternalHealthDao.getAllAbortionWomenCount()
    }

    /**
     * Get all women registered for pregnancy (eligible for PMSMA)
     */
    fun getRegisteredPmsmaWomenList(): Flow<List<PmsmaDomain>> {
        return maternalHealthDao.getAllRegisteredPmsmaWomenList()
            .map { list -> list.map { it.asPmsmaDomainModel() } }
    }

    /**
     * Get count of PMSMA eligible women
     */
    fun getRegisteredPmsmaWomenCount(): Flow<Int> {
        return maternalHealthDao.getAllRegisteredPmsmaWomenCount()
    }

    private suspend fun postDataToAmritServer(ancPostList: MutableSet<ANCPost>): Boolean {
        if (ancPostList.isEmpty()) return false
        val user =
            userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

        try {

            val response = amritApiService.postAncForm(ancPostList.toList())
            val statusCode = response.code()

            if (statusCode == 200) {
                try {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errormessage = jsonObj.getString("errorMessage")
                        if (jsonObj.isNull("statusCode")) throw IllegalStateException("Amrit server not responding properly, Contact Service Administrator!!")
                        val responsestatuscode = jsonObj.getInt("statusCode")

                        when (responsestatuscode) {
                            200 -> {
                                Timber.d("Saved Successfully to server")
                                return true
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName,
                                        user.password
                                    )
                                ) throw SocketTimeoutException()
                            }

                            else -> {
                                Log.d("anc error message", errormessage)
                                throw IOException("Throwing away IO eXcEpTiOn")
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                //server_resp5();
            }
            Timber.w("Bad Response from server, need to check $ancPostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            return postDataToAmritServer(ancPostList)
        } catch (e: JSONException) {
            Timber.d("Caught exception $e here")
            return false
        }
    }


}