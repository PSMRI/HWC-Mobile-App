package org.piramalswasthya.cho.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.InfantRegDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ChildApiPost
import org.piramalswasthya.cho.model.ChildRegDomain
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.Gender
import org.piramalswasthya.cho.model.InfantRegApiPost
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InfantRegDomain
import org.piramalswasthya.cho.model.InfantRegWithPatient
import org.piramalswasthya.cho.model.getIsoDateTimeStringFromLong
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.VillageIdList
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.Locale
import javax.inject.Inject

class InfantRegRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao,
    private val infantRegDao: InfantRegDao
) {
    /**
     * Get list of infants eligible for registration
     * Source of truth: INFANT_REG (downsynced from /infant/getAll), with patient relation.
     */
    fun getListForInfantReg(): Flow<List<InfantRegDomain>> {
        return infantRegDao.getAllRegisteredInfants()
            .map { list ->
                list
                    .map { it.toInfantRegDomain() }
                    .sortedByDescending { it.savedIr?.updatedDate ?: it.savedIr?.createdDate ?: 0L }
            }
    }

    /**
     * Get count of infants eligible for registration
     */
    fun getInfantRegisterCount(): Flow<Int> {
        return infantRegDao.getInfantRegisterCount()
    }

    /**
     * Get all registered infants for child registration list
     */
    fun getRegisteredInfants(): Flow<List<ChildRegDomain>> {
        return infantRegDao.getAllRegisteredInfants()
            .map { list -> list.map { it.asDomainModel() } }
    }

    /**
     * Get count of registered infants
     */
    fun getRegisteredInfantsCount(): Flow<Int> {
        return infantRegDao.getAllRegisteredInfantsCount()
    }

    suspend fun getInfantReg(patientID: String, babyIndex: Int): InfantRegCache? =
        infantRegDao.getInfantReg(patientID, babyIndex)

    suspend fun getInfantRegFromChildPatientID(childPatientID: String): InfantRegCache? =
        infantRegDao.getInfantRegFromChildPatientID(childPatientID)

    suspend fun saveInfantReg(infantRegCache: InfantRegCache) {
        upsertInfantReg(infantRegCache)
    }

    suspend fun upsertInfantReg(infantRegCache: InfantRegCache) {
        val existing = if (infantRegCache.id != 0L) {
            infantRegCache
        } else {
            infantRegDao.getInfantReg(infantRegCache.motherPatientID, infantRegCache.babyIndex)
        }

        if (existing != null && existing.id != 0L) {
            infantRegDao.updateInfantReg(infantRegCache.copy(id = existing.id))
        } else {
            infantRegDao.saveInfantReg(infantRegCache)
        }
    }

    suspend fun getNumBabiesRegistered(patientID: String): Int =
        infantRegDao.getNumBabiesRegistered(patientID)

    suspend fun processNewInfantRegister(): Boolean {
        return withContext(Dispatchers.IO) {
            if (preferenceDao.getLoggedInUser() == null) {
                throw IllegalStateException("No user logged in")
            }

            val infantRegList = infantRegDao.getAllUnprocessedInfantReg()
            if (infantRegList.isEmpty()) return@withContext true

            var hasFailures = false
            infantRegList.forEach { infantReg ->
                val motherPatient = getPatientOrNull(infantReg.motherPatientID)
                val motherBenId = motherPatient?.beneficiaryID

                if (motherBenId == null) {
                    Timber.w("Skipping infant registration ${infantReg.id}: mother beneficiary ID missing")
                    hasFailures = true
                    return@forEach
                }

                val childBenId = infantReg.childPatientID
                    ?.let { getPatientOrNull(it)?.beneficiaryID }
                    ?: 0L
                val childGenderID = infantReg.childPatientID
                    ?.let { getPatientOrNull(it)?.genderID }

                val payload = infantReg.toApiPost(
                    motherBenId = motherBenId,
                    childBenId = childBenId
                )

                infantReg.syncState = SyncState.SYNCING
                infantRegDao.updateInfantReg(infantReg)

                val infantUploadDone = postDataToAmritServer(listOf(payload))
                val childUploadDone = if (infantUploadDone && childBenId > 0L) {
                    val childPayload = infantReg.toChildApiPost(
                        childBenId = childBenId,
                        childGenderID = childGenderID
                    )
                    postChildDataToAmritServer(listOf(childPayload))
                } else {
                    true
                }
                val uploadDone = infantUploadDone && childUploadDone
                if (uploadDone) {
                    infantReg.processed = "P"
                    infantReg.syncState = SyncState.SYNCED
                } else {
                    infantReg.syncState = SyncState.UNSYNCED
                    hasFailures = true
                }
                infantRegDao.updateInfantReg(infantReg)
            }

            return@withContext !hasFailures
        }
    }

    private suspend fun postChildDataToAmritServer(
        childPostList: List<ChildApiPost>
    ): Boolean {
        if (childPostList.isEmpty()) return true
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        return try {
            val response = amritApiService.postChildDetails(childPostList)
            if (response.code() != 200) {
                Timber.w("Child sync failed with HTTP ${response.code()}")
                return false
            }

            val responseString = response.body()?.string()
            if (responseString.isNullOrBlank()) {
                Timber.d("Child saveAll succeeded with empty response body")
                return true
            }
            val jsonObj = JSONObject(responseString)
            val responseStatusCode = jsonObj.optInt("statusCode", -1)
            when (responseStatusCode) {
                200 -> true
                401, 5002 -> {
                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                        throw SocketTimeoutException("Token refreshed. Retrying child sync")
                    }
                    false
                }
                else -> {
                    Timber.w("Child sync failed with response statusCode=$responseStatusCode")
                    false
                }
            }
        } catch (e: SocketTimeoutException) {
            Timber.d("Retrying child sync after token refresh")
            postChildDataToAmritServer(childPostList)
        } catch (e: JSONException) {
            Timber.e(e, "Child sync parse failed")
            false
        } catch (e: Exception) {
            Timber.e(e, "Child sync failed")
            false
        }
    }

    private suspend fun postDataToAmritServer(
        infantRegPostList: List<InfantRegApiPost>
    ): Boolean {
        if (infantRegPostList.isEmpty()) return true

        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        return try {
            val response = amritApiService.postInfantRegForm(infantRegPostList)
            if (response.code() != 200) {
                Timber.w("Infant sync failed with HTTP ${response.code()}")
                return false
            }

            val responseString = response.body()?.string()
            if (responseString.isNullOrBlank()) {
                Timber.d("Infant saveAll succeeded with empty response body")
                return true
            }
            val jsonObj = JSONObject(responseString)
            val responseStatusCode = jsonObj.optInt("statusCode", -1)

            when (responseStatusCode) {
                200 -> true
                401, 5002 -> {
                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                        throw SocketTimeoutException("Token refreshed. Retrying infant sync")
                    }
                    false
                }
                else -> {
                    Timber.w("Infant sync failed with response statusCode=$responseStatusCode")
                    false
                }
            }
        } catch (e: SocketTimeoutException) {
            Timber.d("Retrying infant sync after token refresh")
            postDataToAmritServer(infantRegPostList)
        } catch (e: JSONException) {
            Timber.e(e, "Infant sync failed while parsing response")
            false
        } catch (e: Exception) {
            Timber.e(e, "Infant sync failed")
            false
        }
    }

    private suspend fun getPatientOrNull(patientID: String) =
        runCatching { patientDao.getPatient(patientID) }.getOrNull()

    private fun convertStringToIntList(villageIds: String): List<Int> {
        if (villageIds.trim().isEmpty()) return emptyList()
        return villageIds.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    suspend fun pullInfantsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    convertStringToIntList(user.assignVillageIds ?: ""),
                    preferenceDao.getLastPatientSyncTime()
                )
                val response = amritApiService.getAllInfants(villageList)
                if (response.code() != 200) {
                    Timber.w("Bad response from server for Infant getAll: $response")
                    return@withContext false
                }

                val responseString = response.body()?.string()
                if (responseString.isNullOrBlank()) {
                    Timber.w("Empty response body for Infant getAll")
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
                        for (i in 0 until dataArray.length()) {
                            val networkModel = gson.fromJson(
                                dataArray.getJSONObject(i).toString(),
                                InfantRegApiPost::class.java
                            )
                            val mother = patientDao.getPatientByAnyBeneficiaryId(networkModel.benId)
                            if (mother == null) {
                                Timber.w("No local mother patient found for infant benId=${networkModel.benId}, skipping")
                                continue
                            }
                            val childPatientID = if (networkModel.childBenId > 0) {
                                patientDao.getPatientByAnyBeneficiaryId(networkModel.childBenId)?.patientID
                            } else null

                            val incoming = networkModel.toCacheModel(
                                motherPatientID = mother.patientID,
                                childPatientID = childPatientID
                            ).copy(
                                processed = "P",
                                syncState = SyncState.SYNCED,
                                updatedDate = System.currentTimeMillis(),
                                updatedBy = networkModel.updatedBy?.ifBlank { user.userName } ?: user.userName
                            )
                            val existing = infantRegDao.getInfantReg(mother.patientID, incoming.babyIndex)
                            val merged = if (existing != null) {
                                incoming.copy(
                                    id = existing.id,
                                    createdDate = if (existing.createdDate > 0L) existing.createdDate else incoming.createdDate,
                                    createdBy = if (existing.createdBy.isNotBlank()) existing.createdBy else incoming.createdBy
                                )
                            } else incoming
                            upsertInfantReg(merged)
                            savedCount++
                        }
                        Timber.d("Infant getAll downsync completed, saved=$savedCount received=${dataArray.length()}")
                        return@withContext true
                    }
                    5000 -> {
                        Timber.d("No infant records found on server")
                        return@withContext true
                    }
                    401, 5002 -> {
                        if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                            throw SocketTimeoutException("Token refreshed. Retrying infant getAll")
                        }
                    }
                    else -> {
                        Timber.w("Infant getAll failed: $errorMessage")
                        return@withContext false
                    }
                }
                return@withContext false
            } catch (e: SocketTimeoutException) {
                Timber.d("Retrying infant getAll after token refresh")
                return@withContext pullInfantsFromServer()
            } catch (e: JSONException) {
                Timber.e(e, "Infant getAll parse failed")
                return@withContext false
            } catch (e: Exception) {
                Timber.e(e, "Infant getAll failed")
                return@withContext false
            }
        }
    }

    private fun InfantRegCache.toApiPost(motherBenId: Long, childBenId: Long): InfantRegApiPost {
        return InfantRegApiPost(
            id = id,
            benId = motherBenId,
            childBenId = childBenId,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            gender = mapGender(genderID),
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight ?: 0.0,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = getIsoDateTimeStringFromLong(opv0Dose),
            bcgDose = getIsoDateTimeStringFromLong(bcgDose),
            hepBDose = getIsoDateTimeStringFromLong(hepBDose),
            vitkDose = getIsoDateTimeStringFromLong(vitkDose),
            outcomeAtBirth = outcomeAtBirth,
            typeOfResuscitation = typeOfResuscitation,
            newbornComplications = newbornComplications,
            currentStatusOfBaby = currentStatusOfBaby,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            birthDoseVaccinesGiven = birthDoseVaccinesGiven,
            reasonForNoVaccines = reasonForNoVaccines,
            vitaminKInjectionGiven = vitaminKInjectionGiven,
            reasonForNoVitaminK = reasonForNoVitaminK,
            birthCertificateIssued = birthCertificateIssued,
            createdDate = getIsoDateTimeStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getIsoDateTimeStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }

    private fun InfantRegCache.toChildApiPost(childBenId: Long, childGenderID: Int?): ChildApiPost {
        return ChildApiPost(
            id = id,
            benId = childBenId,
            babyName = babyName,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            gender = mapGender(childGenderID),
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight ?: 0.0,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = getIsoDateTimeStringFromLong(opv0Dose),
            bcgDose = getIsoDateTimeStringFromLong(bcgDose),
            hepBDose = getIsoDateTimeStringFromLong(hepBDose),
            vitkDose = getIsoDateTimeStringFromLong(vitkDose),
            createdDate = getIsoDateTimeStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getIsoDateTimeStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }

    private fun mapGender(genderID: Int?): String? {
        return when (genderID) {
            1 -> Gender.MALE.name
            2 -> Gender.FEMALE.name
            3 -> Gender.TRANSGENDER.name
            else -> null
        }
    }

    private fun InfantRegWithPatient.toInfantRegDomain(): InfantRegDomain {
        val fallbackDelivery = DeliveryOutcomeCache(
            patientID = motherPatient.patientID,
            isActive = true,
            dateOfDelivery = infant.createdDate,
            createdBy = infant.createdBy,
            updatedBy = infant.updatedBy,
            syncState = infant.syncState
        )
        return InfantRegDomain(
            motherPatient = motherPatient,
            babyIndex = infant.babyIndex,
            deliveryOutcome = fallbackDelivery,
            savedIr = infant
        )
    }

    private fun InfantRegApiPost.toCacheModel(
        motherPatientID: String,
        childPatientID: String?
    ): InfantRegCache {
        val genderID = when (gender?.trim()?.lowercase(Locale.ENGLISH)) {
            "male" -> 1
            "female" -> 2
            "transgender" -> 3
            else -> null
        }
        return InfantRegCache(
            id = id,
            motherPatientID = motherPatientID,
            childPatientID = childPatientID,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            genderID = genderID,
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = org.piramalswasthya.cho.network.getLongFromDate(opv0Dose),
            bcgDose = org.piramalswasthya.cho.network.getLongFromDate(bcgDose),
            hepBDose = org.piramalswasthya.cho.network.getLongFromDate(hepBDose),
            vitkDose = org.piramalswasthya.cho.network.getLongFromDate(vitkDose),
            outcomeAtBirth = outcomeAtBirth,
            typeOfResuscitation = typeOfResuscitation,
            newbornComplications = newbornComplications,
            currentStatusOfBaby = currentStatusOfBaby,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            birthDoseVaccinesGiven = birthDoseVaccinesGiven,
            reasonForNoVaccines = reasonForNoVaccines,
            vitaminKInjectionGiven = vitaminKInjectionGiven,
            reasonForNoVitaminK = reasonForNoVitaminK,
            birthCertificateIssued = birthCertificateIssued,
            processed = "P",
            createdBy = createdBy ?: "system",
            createdDate = org.piramalswasthya.cho.network.getLongFromDate(createdDate),
            updatedBy = updatedBy ?: (createdBy ?: "system"),
            updatedDate = org.piramalswasthya.cho.network.getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}
