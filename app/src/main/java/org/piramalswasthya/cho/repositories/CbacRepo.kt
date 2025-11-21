package org.piramalswasthya.cho.repositories

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.CbacDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.network.AmritApiService

import org.piramalswasthya.cho.model.CbacCache
import org.piramalswasthya.cho.model.CbacRequest
import org.piramalswasthya.cho.model.CbacVisitDetails

import org.piramalswasthya.cho.model.VisitDetailsWrapper
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.NurseDataRequest
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor

import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

class CbacRepo @Inject constructor(
    @ApplicationContext var context: Context,
    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val amritApiService: AmritApiService,
    private val prefDao: PreferenceDao,
    private val cbacDao: CbacDao,
    private val patientRepo: PatientRepo,
    private val benFlowDao: BenFlowDao
) {

    suspend fun saveCbacData(cbacCache: CbacCache,): Boolean {
        return withContext(Dispatchers.IO) {

            val user =
                prefDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                cbacCache.apply {
                    createdBy = user.userName
                    createdDate = System.currentTimeMillis()
                    serverUpdatedStatus = 0
                    cbac_tracing_all_fm =
                        if (cbac_sufferingtb_pos == 1 || cbac_antitbdrugs_pos == 1)
                            "1"
                        else
                            "0"
                    cbac_sputemcollection = if (cbac_tbhistory_pos == 1 ||
                        cbac_coughing_pos == 1 ||
                        cbac_bloodsputum_pos == 1 ||
                        cbac_fivermore_pos == 1 ||
                        cbac_loseofweight_pos == 1 ||
                        cbac_nightsweats_pos == 1
                    )
                        "1"
                    else
                        "0"
                    Processed = "N"
//                    ProviderServiceMapID = user.serviceMapId
//                    VanID = user.vanId

                }

                database.cbacDao.upsert(cbacCache)
                true
            } catch (e: java.lang.Exception) {
                Timber.d("Error : $e raised at saveCbacData")
                false
            }
        }
    }
//
    suspend fun getCbacCacheFromId(cbacId: Int): CbacCache {
        return withContext(Dispatchers.IO) {
            database.cbacDao.getCbacFromBenId(cbacId)
                ?: throw IllegalStateException("No CBAC entry found!")
        }

    }

    suspend fun getLastFilledCbac(benId: String): CbacCache? {
        return withContext(Dispatchers.IO) {
            database.cbacDao.getLastFilledCbacFromBenId(benId = benId)
        }
    }
    enum class Gender(val id: Int) {
        MALE(1),
        FEMALE(2),
        TRANSGENDER(3);

        companion object {
            fun fromId(id: Int): Gender {
                return values().find { it.id == id } ?: MALE
            }
        }
    }


    suspend fun pushUnSyncedCbacRecords(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

            val cbacCacheList: List<CbacCache> =
                cbacDao.getAllUnprocessedCbac(SyncState.UNSYNCED)

            if (cbacCacheList.isEmpty()) return@withContext true

            var allSynced = true

            cbacCacheList.forEach { cache ->
                val patient = patientRepo.getPatient(cache.patId)
                val genderEnum = patient.genderID?.let { Gender.fromId(it) }



                if (patient.beneficiaryID != null) {
                    val cbacPostNew = genderEnum?.let {
                        cache.asPostModel(
                            benGender = it,
                            resources = context.resources,
                        )
                    }

                    cbacPostNew?.let { cbac ->
                        val request = CbacRequest(
                            visitDetails = VisitDetailsWrapper(
                            visitDetails = CbacVisitDetails(
                                beneficiaryRegID = patient.beneficiaryRegID!!,
                                providerServiceMapID = userRepo.getLoggedInUser()!!.serviceMapId,
                                visitNo = null ,
                                visitReason = "New Chief Complaint",
                                visitCategory = "NCD screening",
                                IdrsOrCbac = "CBAC",
                                createdBy = user.userName,
                                vanID = userRepo.getLoggedInUser()!!.vanId,
                                parkingPlaceID = userRepo.getLoggedInUser()!!.parkingPlaceId,
                                subVisitCategory = null,
                                pregnancyStatus = null,
                                followUpForFpMethod = null,
                                sideEffects = null,
                                otherSideEffects = null,
                                fileIDs = null,
                                reportFilePath = null,
                                otherFollowUpForFpMethod = null,
                                rCHID = null,
                                healthFacilityType = null,
                                healthFacilityLocation = null)
                            ),
                            cbac = cbac,
                            benFlowID = patient.beneficiaryRegID!!,
                            beneficiaryID = patient.beneficiaryID!!,
                            sessionID = 3,
                            parkingPlaceID = userRepo.getLoggedInUser()!!.parkingPlaceId,
                            createdBy = user.userName,
                            vanID = userRepo.getLoggedInUser()!!.vanId,
                            beneficiaryRegID = patient.beneficiaryRegID!!,
                            benVisitID = null,
                            providerServiceMapID = userRepo.getLoggedInUser()!!.serviceMapId
                        )

                        try {
                            val response = amritApiService.postCbacData(request)
                            if (response.isSuccessful) {
                                val responseString = response.body()?.string()
                                if (responseString != null) {
                                    val jsonObj = JSONObject(responseString)
                                    val responseStatusCode = jsonObj.getInt("statusCode")

                                    when (responseStatusCode) {
                                        200 -> {
                                            updateSyncStatusCbac(cache) // ✅ ek record sync update
                                        }
                                        5002 -> {
                                            if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                                throw SocketTimeoutException("Refreshed Token!")
                                            } else throw IllegalStateException("User Logged out!!")
                                        }
                                        5000 -> {
                                            val errorMessage = jsonObj.getString("errorMessage")
                                            Log.d("CBAC sync failed", errorMessage)
                                            allSynced = false
                                        }
                                        else -> {
                                            allSynced = false
                                            Timber.d("Unknown response $responseStatusCode")
                                        }
                                    }
                                } else {
                                    allSynced = false
                                }
                            } else {
                                allSynced = false
                                Log.d("CBAC sync", "HTTP error: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            allSynced = false
                            Timber.e(e, "CBAC sync failed for patId=${cache.patId}")
                        }
                    }
                }
            }

            return@withContext allSynced
        }
    }

    private suspend fun updateSyncStatusCbac(cbac: CbacCache) {
        cbac.syncState = SyncState.SYNCED
        cbac.Processed = "P"
        cbacDao.upsert(cbac)
    }

    private suspend fun getAndSaveCbacDataToDb(benFlow: BenFlow): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val cbacRequest = NurseDataRequest(benRegID = benFlow.beneficiaryRegID!!, visitCode = benFlow.visitCode!!)

            val response = amritApiService.getCbacData(cbacRequest)
            val responseBody = response.body()?.string()

            Timber.tag("API Response CBAC ,${responseBody.toString()}")
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }

                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getAndSaveCbacDataToDb(benFlow)
                }
            )
        }

    }

    suspend fun downloadAndSyncCbacRecords(): Boolean {
        val benFlows = benFlowDao.getAllBenFlows()
        benFlows.forEach { benFlow ->

            val result = getAndSaveCbacDataToDb(benFlow)
            if (result is NetworkResult.Error) {
                return false
            }
        }
        return true
    }

}