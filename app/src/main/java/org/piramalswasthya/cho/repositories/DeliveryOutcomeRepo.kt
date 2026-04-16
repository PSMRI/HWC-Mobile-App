package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.helpers.setToStartOfTheDay
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.DeliveryOutcomePost
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.VillageIdList
//import org.piramalswasthya.cho.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeliveryOutcomeRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val deliveryOutcomeDao: DeliveryOutcomeDao,
    private val patientDao: PatientDao
) {

    suspend fun getDeliveryOutcome(patientID: String): DeliveryOutcomeCache? {
        return withContext(Dispatchers.IO) {
            deliveryOutcomeDao.getDeliveryOutcome(patientID)
        }
    }

    suspend fun saveDeliveryOutcome(deliveryOutcomeCache: DeliveryOutcomeCache) {
        withContext(Dispatchers.IO) {
            // Upsert pattern: check for existing record by patientID and reuse its id if found
            val existing = deliveryOutcomeDao.getDeliveryOutcome(deliveryOutcomeCache.patientID)

            if (existing != null) {
                // Reuse existing record's id and update
                val updatedCache = deliveryOutcomeCache.copy(id = existing.id)
                val rowsAffected = deliveryOutcomeDao.updateDeliveryOutcome(updatedCache)
                if (rowsAffected == 0) {
                    // Update failed (record not found by id), insert instead
                    deliveryOutcomeDao.saveDeliveryOutcome(updatedCache)
                }
            } else if (deliveryOutcomeCache.id != 0L) {
                // Has id but not found in DB, try update first
                val rowsAffected = deliveryOutcomeDao.updateDeliveryOutcome(deliveryOutcomeCache)
                if (rowsAffected == 0) {
                    // Update failed (id doesn't exist), insert instead
                    deliveryOutcomeDao.saveDeliveryOutcome(deliveryOutcomeCache)
                }
            } else {
                // New record, insert
                deliveryOutcomeDao.saveDeliveryOutcome(deliveryOutcomeCache)
            }
        }
    }

    private fun convertStringToIntList(villageIds: String): List<Int> {
        if (villageIds.trim().isEmpty()) return emptyList()
        return villageIds.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    suspend fun processNewDeliveryOutcomes(): Boolean {
        return withContext(Dispatchers.IO) {
            val deliveryList = deliveryOutcomeDao.getAllUnprocessedDeliveryOutcomes()
            if (deliveryList.isEmpty()) {
                Timber.d("No unsynced Delivery Outcome records found for upload")
                return@withContext true
            }

            var hasFailures = false
            val payload = mutableSetOf<DeliveryOutcomePost>()

            deliveryList.forEach { record ->
                payload.clear()
                val patient = patientDao.getPatient(record.patientID)
                val benId = patient.beneficiaryID
                if (benId == null || benId == 0L) {
                    Timber.w("Skipping Delivery Outcome upload for ${record.patientID}: beneficiaryID missing")
                    hasFailures = true
                    return@forEach
                }

                record.syncState = SyncState.SYNCING
                deliveryOutcomeDao.updateDeliveryOutcome(record)
                payload.add(record.asPostModel(benId))

                val uploaded = postDeliveryOutcomeToAmritServer(payload)
                if (uploaded) {
                    record.processed = "P"
                    record.syncState = SyncState.SYNCED
                } else {
                    record.syncState = SyncState.UNSYNCED
                    hasFailures = true
                }
                deliveryOutcomeDao.updateDeliveryOutcome(record)
            }

            return@withContext !hasFailures
        }
    }

    private suspend fun postDeliveryOutcomeToAmritServer(payload: MutableSet<DeliveryOutcomePost>): Boolean {
        if (payload.isEmpty()) return false
        val user = userRepo.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in!!")
        try {
            val response = amritApiService.postDeliveryOutcomeForm(payload.toList())
            val statusCode = response.code()
            if (statusCode != 200) {
                Timber.w("Bad response from server for DeliveryOutcome saveAll, code=$statusCode")
                return false
            }

            val responseString = response.body()?.string()
            if (responseString.isNullOrBlank()) {
                Timber.d("DeliveryOutcome saveAll succeeded with empty response body")
                return true
            }

            val jsonObj = try {
                JSONObject(responseString)
            } catch (e: Exception) {
                Timber.w("DeliveryOutcome saveAll returned non-JSON body, treating as success: $responseString")
                return true
            }

            val responseStatusCode = jsonObj.optInt("statusCode", 200)
            val errorMessage = jsonObj.optString("errorMessage")
            when (responseStatusCode) {
                200 -> return true
                5002 -> {
                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                        throw SocketTimeoutException()
                    }
                }
                else -> {
                    Timber.w("DeliveryOutcome saveAll failed: $errorMessage")
                    return false
                }
            }
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught timeout for DeliveryOutcome saveAll $e; retrying")
            return postDeliveryOutcomeToAmritServer(payload)
        } catch (e: JSONException) {
            Timber.d("Caught JSON exception for DeliveryOutcome saveAll $e")
            return false
        }
    }

    suspend fun pullDeliveryOutcomesFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    convertStringToIntList(user.assignVillageIds ?: ""),
                    preferenceDao.getLastPatientSyncTime()
                )
                val response = amritApiService.getAllDeliveryOutcomes(villageList)
                if (response.code() != 200) {
                    Timber.w("Bad response from server for DeliveryOutcome getAll: $response")
                    return@withContext false
                }

                val responseString = response.body()?.string()
                if (responseString.isNullOrBlank()) {
                    Timber.w("Empty response body for DeliveryOutcome getAll")
                    return@withContext false
                }

                val jsonObj = JSONObject(responseString)
                val responseStatusCode = jsonObj.optInt("statusCode", 200)
                val errorMessage = jsonObj.optString("errorMessage")
                when (responseStatusCode) {
                    200 -> {
                        val dataArray = when (val dataNode = jsonObj.opt("data")) {
                            is org.json.JSONArray -> dataNode
                            is org.json.JSONObject -> dataNode.optJSONArray("data")
                            else -> null
                        } ?: org.json.JSONArray()

                        val gson = Gson()
                        var savedCount = 0
                        var skippedNoPatientCount = 0
                        for (i in 0 until dataArray.length()) {
                            val networkModel = gson.fromJson(
                                dataArray.getJSONObject(i).toString(),
                                DeliveryOutcomePost::class.java
                            )
                            if (networkModel.benId == 0L) {
                                Timber.w("Skipping DeliveryOutcome getAll item with invalid benId at index=$i")
                                continue
                            }

                            val patient = patientDao.getPatientByAnyBeneficiaryId(networkModel.benId)
                                ?: ensurePatientPlaceholderForDeliveryOutcome(networkModel.benId)
                            if (patient == null) {
                                Timber.w("No local patient found for DeliveryOutcome benId=${networkModel.benId}, skipping")
                                skippedNoPatientCount++
                                continue
                            }

                            val incoming = networkModel.toDeliveryCache(patient.patientID).copy(
                                processed = "P",
                                syncState = SyncState.SYNCED,
                                updatedDate = System.currentTimeMillis(),
                                updatedBy = networkModel.updatedBy.ifBlank { user.userName }
                            )
                            val existing = deliveryOutcomeDao.getDeliveryOutcome(patient.patientID)
                            val merged = if (existing != null) {
                                mergeDeliveryOutcome(existing, incoming)
                            } else incoming

                            saveDeliveryOutcome(merged)
                            savedCount++
                        }
                        Timber.d("DeliveryOutcome getAll downsync completed, saved=$savedCount skippedNoPatient=$skippedNoPatientCount received=${dataArray.length()}")
                        return@withContext true
                    }
                    5000 -> {
                        Timber.d("No Delivery Outcome records found on server")
                        return@withContext true
                    }
                    5002 -> {
                        if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                            throw SocketTimeoutException()
                        }
                    }
                    else -> {
                        Timber.w("DeliveryOutcome getAll failed: $errorMessage")
                        throw IOException("DeliveryOutcome getAll failed with statusCode=$responseStatusCode")
                    }
                }
                return@withContext false
            } catch (e: SocketTimeoutException) {
                Timber.d("Caught timeout for DeliveryOutcome getAll $e; retrying")
                return@withContext pullDeliveryOutcomesFromServer()
            } catch (e: JSONException) {
                Timber.d("Caught JSON exception for DeliveryOutcome getAll $e")
                return@withContext false
            }
        }
    }

    private suspend fun ensurePatientPlaceholderForDeliveryOutcome(benId: Long): Patient? {
        if (benId <= 0L) return null
        val existing = patientDao.getPatientByAnyBeneficiaryId(benId)
        if (existing != null) return existing

        val placeholderId = "delivery-patient-$benId"
        val now = Date()
        val placeholder = Patient(
            patientID = placeholderId,
            firstName = "Patient",
            lastName = benId.toString(),
            dob = null,
            age = null,
            ageUnitID = null,
            maritalStatusID = null,
            spouseName = null,
            ageAtMarriage = null,
            phoneNo = null,
            genderID = null,
            registrationDate = now,
            stateID = null,
            districtID = null,
            blockID = null,
            districtBranchID = null,
            communityID = null,
            religionID = null,
            parentName = null,
            syncState = SyncState.SYNCED,
            beneficiaryID = benId,
            beneficiaryRegID = null,
            benImage = null,
            statusOfWomanID = null,
            isNewAbha = false,
            healthIdDetails = null,
            labTechnicianFlag = 0,
            faceEmbedding = null
        )
        return runCatching {
            patientDao.insertPatient(placeholder)
            patientDao.getPatient(placeholderId)
        }.onFailure { err ->
            Timber.w(err, "Unable to create delivery patient placeholder for benId=$benId")
        }.getOrNull()
    }

    private fun mergeDeliveryOutcome(
        existing: DeliveryOutcomeCache,
        incoming: DeliveryOutcomeCache
    ): DeliveryOutcomeCache {
        return incoming.copy(
            id = existing.id,
            patientID = existing.patientID,
            isActive = incoming.isActive,
            dateOfDelivery = incoming.dateOfDelivery ?: existing.dateOfDelivery,
            timeOfDelivery = incoming.timeOfDelivery ?: existing.timeOfDelivery,
            placeOfDelivery = incoming.placeOfDelivery ?: existing.placeOfDelivery,
            typeOfDelivery = incoming.typeOfDelivery ?: existing.typeOfDelivery,
            hadComplications = incoming.hadComplications ?: existing.hadComplications,
            complication = incoming.complication ?: existing.complication,
            causeOfDeath = incoming.causeOfDeath ?: existing.causeOfDeath,
            otherCauseOfDeath = incoming.otherCauseOfDeath ?: existing.otherCauseOfDeath,
            otherComplication = incoming.otherComplication ?: existing.otherComplication,
            deliveryOutcome = incoming.deliveryOutcome ?: existing.deliveryOutcome,
            liveBirth = incoming.liveBirth ?: existing.liveBirth,
            stillBirth = incoming.stillBirth ?: existing.stillBirth,
            dateOfDischarge = incoming.dateOfDischarge ?: existing.dateOfDischarge,
            timeOfDischarge = incoming.timeOfDischarge ?: existing.timeOfDischarge,
            isJSYBenificiary = incoming.isJSYBenificiary ?: existing.isJSYBenificiary,
            gestationalAgeAtDelivery = incoming.gestationalAgeAtDelivery ?: existing.gestationalAgeAtDelivery,
            deliveryConductedBy = incoming.deliveryConductedBy ?: existing.deliveryConductedBy,
            modeOfDelivery = incoming.modeOfDelivery ?: existing.modeOfDelivery,
            indicationForLSCS = incoming.indicationForLSCS ?: existing.indicationForLSCS,
            indicationForLSCSOther = incoming.indicationForLSCSOther ?: existing.indicationForLSCSOther,
            privateHospitalName = incoming.privateHospitalName ?: existing.privateHospitalName,
            motherCondition = incoming.motherCondition ?: existing.motherCondition,
            maternalComplications = incoming.maternalComplications ?: existing.maternalComplications,
            motherCurrentlyAdmitted = incoming.motherCurrentlyAdmitted ?: existing.motherCurrentlyAdmitted,
            isDeath = incoming.isDeath ?: existing.isDeath,
            isDeathValue = incoming.isDeathValue ?: existing.isDeathValue,
            dateOfDeath = incoming.dateOfDeath ?: existing.dateOfDeath,
            placeOfDeath = incoming.placeOfDeath ?: existing.placeOfDeath,
            placeOfDeathId = incoming.placeOfDeathId ?: existing.placeOfDeathId,
            otherPlaceOfDeath = incoming.otherPlaceOfDeath ?: existing.otherPlaceOfDeath,
            processed = incoming.processed ?: existing.processed,
            createdBy = if (existing.createdBy.isNotBlank()) existing.createdBy else incoming.createdBy,
            createdDate = if (existing.createdDate > 0L) existing.createdDate else incoming.createdDate,
            updatedBy = incoming.updatedBy,
            updatedDate = incoming.updatedDate,
            syncState = incoming.syncState
        )
    }

}
