package org.piramalswasthya.cho.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONArray
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
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.getIsoDateTimeStringFromLong
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.VillageIdList
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.Date
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
     * Source of truth: combined DELIVERY_OUTCOME + INFANT_REG.
     * - Generates rows from total delivery outcome count.
     * - Marks only live-birth rows as registerable.
     * - Maps synced infant rows onto generated indices for View state.
     */
    fun getListForInfantReg(): Flow<List<InfantRegDomain>> {
        return infantRegDao.getListForInfantRegister()
            .map { list ->
                list
                    .flatMap { it.asDomainModel() }
                    .sortedByDescending { it.deliveryOutcome.dateOfDelivery ?: 0L }
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
            .map { list ->
                list
                    .map { it.asDomainModel() }
            }
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
        // IMPORTANT:
        // Downsync payload can contain non-zero server-side IDs that do not exist in local DB.
        // So we must not assume `id != 0` means "already present locally".
        // Local uniqueness for this module is motherPatientID + babyIndex.
        val existing = infantRegDao.getInfantReg(
            infantRegCache.motherPatientID,
            infantRegCache.babyIndex
        )

        if (existing != null) {
            infantRegDao.updateInfantReg(infantRegCache.copy(id = existing.id))
        } else {
            infantRegDao.saveInfantReg(infantRegCache.copy(id = 0))
        }
    }

    suspend fun getNumBabiesRegistered(patientID: String): Int =
        infantRegDao.getNumBabiesRegistered(patientID)

    /**
     * Creates/ensures infant placeholder rows in INFANT_REG based on Delivery Outcome count.
     * We keep placeholders as processed/synced so they are local list entries until user fills details.
     */
    suspend fun ensureInfantPlaceholdersForDeliveryOutcome(
        motherPatientID: String,
        motherName: String?,
        infantCount: Int,
        userName: String
    ) {
        if (infantCount <= 0) return
        val safeMotherName = motherName?.trim().takeUnless { it.isNullOrBlank() } ?: "Mother"
        val now = System.currentTimeMillis()

        for (index in 0 until infantCount) {
            val existing = infantRegDao.getInfantReg(motherPatientID, index)
            if (existing != null) continue

            val placeholder = InfantRegCache(
                motherPatientID = motherPatientID,
                babyIndex = index,
                babyName = "baby ${index + 1} of $safeMotherName",
                isActive = true,
                processed = "Y",
                createdBy = userName,
                createdDate = now,
                updatedBy = userName,
                updatedDate = now,
                syncState = SyncState.SYNCED
            )
            upsertInfantReg(placeholder)
        }
    }

    suspend fun processNewInfantRegister(): Boolean {
        return withContext(Dispatchers.IO) {
            if (preferenceDao.getLoggedInUser() == null) {
                throw IllegalStateException("No user logged in")
            }

            val infantRegList = infantRegDao.getAllUnprocessedInfantReg()
            if (infantRegList.isEmpty()) return@withContext true

            val deduplicatedInfantRegs = infantRegList
                .groupBy { it.motherPatientID to it.babyIndex }
                .map { (_, records) ->
                    records.maxWithOrNull(
                        compareBy<InfantRegCache> { it.updatedDate }
                            .thenBy { it.createdDate }
                            .thenBy { it.id }
                    ) ?: records.first()
                }

            var hasFailures = false
            deduplicatedInfantRegs.forEach { infantReg ->
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

                val payload = infantReg.toApiPost(
                    motherBenId = motherBenId,
                    childBenId = childBenId
                )

                infantReg.syncState = SyncState.SYNCING
                infantRegDao.updateInfantReg(infantReg)

                val uploadDone = postDataToAmritServer(listOf(payload))
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

    suspend fun syncChildRegistration(infantReg: InfantRegCache): Boolean {
        return withContext(Dispatchers.IO) {
            val motherPatient = getPatientOrNull(infantReg.motherPatientID)
            val motherBenId = motherPatient?.beneficiaryID
            if (motherBenId == null || motherBenId <= 0L) {
                Timber.w("Skipping child registration sync ${infantReg.id}: mother beneficiary ID missing")
                return@withContext false
            }

            val childBenId = infantReg.childPatientID
                ?.let { getPatientOrNull(it)?.beneficiaryID }
                ?.takeIf { it > 0L }
                ?: motherBenId

            val childGenderID = infantReg.childPatientID
                ?.let { getPatientOrNull(it)?.genderID }
                ?: infantReg.genderID

            val childPayload = infantReg.toChildApiPost(
                childBenId = childBenId,
                childGenderID = childGenderID
            )
            val syncDone = postChildDataToAmritServer(listOf(childPayload))
            if (syncDone) {
                val now = System.currentTimeMillis()
                val updated = infantReg.copy(
                    processed = "C",
                    syncState = SyncState.SYNCED,
                    updatedDate = now
                )
                upsertInfantReg(updated)
            }
            syncDone
        }
    }

    private suspend fun postChildDataToAmritServer(
        childPostList: List<ChildApiPost>
    ): Boolean {
        if (childPostList.isEmpty()) return true
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")
        var refreshed = false
        while (true) {
            try {
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
                    200 -> return true
                    401, 5002 -> {
                        if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                            refreshed = true
                            Timber.d("Retrying child sync after token refresh")
                            continue
                        }
                        return false
                    }
                    else -> {
                        Timber.w("Child sync failed with response statusCode=$responseStatusCode")
                        return false
                    }
                }
            } catch (e: SocketTimeoutException) {
                if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                    refreshed = true
                    Timber.d("Retrying child sync after token refresh")
                    continue
                }
                Timber.e(e, "Child sync failed")
                return false
            } catch (e: JSONException) {
                Timber.e(e, "Child sync parse failed")
                return false
            } catch (e: Exception) {
                Timber.e(e, "Child sync failed")
                return false
            }
        }
    }

    private suspend fun postDataToAmritServer(
        infantRegPostList: List<InfantRegApiPost>
    ): Boolean {
        if (infantRegPostList.isEmpty()) return true

        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")
        var refreshed = false
        while (true) {
            try {
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
                    200 -> return true
                    401, 5002 -> {
                        if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                            refreshed = true
                            Timber.d("Retrying infant sync after token refresh")
                            continue
                        }
                        return false
                    }
                    else -> {
                        Timber.w("Infant sync failed with response statusCode=$responseStatusCode")
                        return false
                    }
                }
            } catch (e: SocketTimeoutException) {
                if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                    refreshed = true
                    Timber.d("Retrying infant sync after token refresh")
                    continue
                }
                Timber.e(e, "Infant sync failed")
                return false
            } catch (e: JSONException) {
                Timber.e(e, "Infant sync failed while parsing response")
                return false
            } catch (e: Exception) {
                Timber.e(e, "Infant sync failed")
                return false
            }
        }
    }

    private suspend fun getPatientOrNull(patientID: String) =
        runCatching { patientDao.getPatient(patientID) }.getOrNull()

    suspend fun pullInfantsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            var refreshed = false
            while (true) {
                try {
                val villageList = VillageIdList(
                    RepositorySyncUtils.parseVillageIds(user.assignVillageIds ?: ""),
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
                        val dataArray = extractInfantDataArray(jsonObj)

                        val gson = Gson()
                        var savedCount = 0
                        var skippedNoMotherCount = 0
                        var skippedInvalidPayloadCount = 0
                        var failedSaveCount = 0
                        for (i in 0 until dataArray.length()) {
                            try {
                                val rawObj = extractInfantItemObject(dataArray, i)
                                if (rawObj == null) {
                                    skippedInvalidPayloadCount++
                                    continue
                                }
                                val normalizedObj = normalizeInfantPayload(rawObj)
                                val networkModel = gson.fromJson(
                                    normalizedObj.toString(),
                                    InfantRegApiPost::class.java
                                )
                                if (networkModel.benId <= 0L) {
                                    Timber.w("Invalid infant payload at index=$i: benId=${networkModel.benId}, skipping")
                                    skippedInvalidPayloadCount++
                                    continue
                                }
                                val mother = patientDao.getPatientByAnyBeneficiaryId(networkModel.benId)
                                    ?: ensureMotherPlaceholderForInfant(benId = networkModel.benId)
                                if (mother == null) {
                                    Timber.w("No local mother patient found for infant benId=${networkModel.benId}, skipping")
                                    skippedNoMotherCount++
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
                                    if (existing.syncState == SyncState.SYNCED) {
                                        incoming.copy(
                                            id = existing.id,
                                            createdDate = if (existing.createdDate > 0L) existing.createdDate else incoming.createdDate,
                                            createdBy = if (existing.createdBy.isNotBlank()) existing.createdBy else incoming.createdBy
                                        )
                                    } else {
                                        existing.copy(
                                            id = existing.id,
                                            childPatientID = existing.childPatientID ?: incoming.childPatientID
                                        )
                                    }
                                } else incoming
                                upsertInfantReg(merged)
                                savedCount++
                            } catch (e: Exception) {
                                failedSaveCount++
                                Timber.w(e, "Failed saving infant payload index=$i")
                            }
                        }
                        Timber.d(
                            "Infant getAll downsync completed, saved=$savedCount " +
                                "skippedNoMother=$skippedNoMotherCount " +
                                "skippedInvalidPayload=$skippedInvalidPayloadCount failedSave=$failedSaveCount " +
                                "received=${dataArray.length()}"
                        )
                        val downsyncSuccess =
                            failedSaveCount == 0 &&
                                skippedNoMotherCount == 0 &&
                                skippedInvalidPayloadCount == 0
                        return@withContext downsyncSuccess
                    }
                    5000 -> {
                        Timber.d("No infant records found on server")
                        return@withContext true
                    }
                    401, 5002 -> {
                        if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                            refreshed = true
                            Timber.d("Retrying infant getAll after token refresh")
                            continue
                        }
                        return@withContext false
                    }
                    else -> {
                        Timber.w("Infant getAll failed: $errorMessage")
                        return@withContext false
                    }
                }
                return@withContext false
                } catch (e: SocketTimeoutException) {
                    if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                        refreshed = true
                        Timber.d("Retrying infant getAll after token refresh")
                        continue
                    }
                    Timber.e(e, "Infant getAll failed")
                    return@withContext false
                } catch (e: JSONException) {
                    Timber.e(e, "Infant getAll parse failed")
                    return@withContext false
                } catch (e: Exception) {
                    Timber.e(e, "Infant getAll failed")
                    return@withContext false
                }
            }
            return@withContext false
        }
    }

    suspend fun pullChildrenFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            var refreshed = false
            while (true) {
                try {
                val villageList = VillageIdList(
                    RepositorySyncUtils.parseVillageIds(user.assignVillageIds ?: ""),
                    preferenceDao.getLastPatientSyncTime()
                )
                val response = amritApiService.getAllChildren(villageList)
                if (response.code() != 200) {
                    Timber.w("Bad response from server for Child getAll: $response")
                    return@withContext false
                }

                val responseString = response.body()?.string()
                if (responseString.isNullOrBlank()) {
                    Timber.w("Empty response body for Child getAll")
                    return@withContext false
                }

                val jsonObj = JSONObject(responseString)
                val responseStatusCode = jsonObj.optInt("statusCode", 200)
                val errorMessage = jsonObj.optString("errorMessage")
                when (responseStatusCode) {
                    200 -> {
                        val dataArray = extractInfantDataArray(jsonObj)
                        val gson = Gson()
                        var savedCount = 0
                        var skippedNoMotherCount = 0
                        var skippedInvalidPayloadCount = 0
                        var failedSaveCount = 0

                        for (i in 0 until dataArray.length()) {
                            try {
                                val rawObj = extractInfantItemObject(dataArray, i)
                                if (rawObj == null) {
                                    skippedInvalidPayloadCount++
                                    continue
                                }
                                val normalizedObj = normalizeChildPayload(rawObj)
                                val networkModel = gson.fromJson(
                                    normalizedObj.toString(),
                                    ChildApiPost::class.java
                                )
                                if (networkModel.benId <= 0L) {
                                    skippedInvalidPayloadCount++
                                    continue
                                }

                                val mother = patientDao.getPatientByAnyBeneficiaryId(networkModel.benId)
                                    ?: ensureMotherPlaceholderForInfant(networkModel.benId)
                                if (mother == null) {
                                    skippedNoMotherCount++
                                    continue
                                }

                                val existingRegs = infantRegDao.getAllInfantRegs(setOf(mother.patientID))
                                    .filter { it.isActive }
                                    .sortedWith(
                                        compareBy<InfantRegCache> { it.babyIndex }
                                            .thenByDescending { it.updatedDate }
                                    )
                                val resolvedExisting = resolveChildRecord(existingRegs, networkModel)
                                val resolvedBabyIndex = resolvedExisting?.babyIndex
                                    ?: existingRegs.firstOrNull { !it.hasRegistrationData() }?.babyIndex
                                    ?: existingRegs.size

                                val merged = mergeChildDownsync(
                                    existing = resolvedExisting,
                                    motherPatientID = mother.patientID,
                                    child = networkModel,
                                    babyIndex = resolvedBabyIndex,
                                    userName = user.userName
                                )

                                upsertInfantReg(merged)
                                savedCount++
                            } catch (e: Exception) {
                                failedSaveCount++
                                Timber.w(e, "Failed saving child payload index=$i")
                            }
                        }

                        Timber.d(
                            "Child getAll downsync completed, saved=$savedCount " +
                                "skippedNoMother=$skippedNoMotherCount " +
                                "skippedInvalidPayload=$skippedInvalidPayloadCount failedSave=$failedSaveCount " +
                                "received=${dataArray.length()}"
                        )
                        return@withContext true
                    }
                    5000 -> {
                        Timber.d("No child records found on server")
                        return@withContext true
                    }
                    401, 5002 -> {
                        if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                            refreshed = true
                            Timber.d("Retrying child getAll after token refresh")
                            continue
                        }
                        return@withContext false
                    }
                    else -> {
                        Timber.w("Child getAll failed: $errorMessage")
                        return@withContext false
                    }
                }
                return@withContext false
                } catch (e: SocketTimeoutException) {
                    if (!refreshed && userRepo.refreshTokenTmc(user.userName, user.password)) {
                        refreshed = true
                        Timber.d("Retrying child getAll after token refresh")
                        continue
                    }
                    Timber.e(e, "Child getAll failed")
                    return@withContext false
                } catch (e: JSONException) {
                    Timber.e(e, "Child getAll parse failed")
                    return@withContext false
                } catch (e: Exception) {
                    Timber.e(e, "Child getAll failed")
                    return@withContext false
                }
            }
            return@withContext false
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

    private fun extractInfantDataArray(root: JSONObject): JSONArray {
        // Try strict path first for current contracts.
        val dataNode = root.opt("data")
        if (dataNode is JSONArray) return dataNode
        if (dataNode is String) {
            parseJsonArrayFromString(dataNode)?.let { return it }
            parseJsonObjectFromString(dataNode)?.let { obj ->
                val nested = extractInfantDataArray(JSONObject().put("data", obj))
                if (nested.length() > 0) return nested
            }
        }
        if (dataNode is JSONObject) {
            val keys = listOf("data", "response", "records", "content", "list", "items")
            for (key in keys) {
                val arr = dataNode.optJSONArray(key)
                if (arr != null) return arr
                val text = dataNode.optString(key, "")
                parseJsonArrayFromString(text)?.let { return it }
            }
        }
        // Fallback: recursively scan full response for first JSON array payload.
        findFirstJsonArray(root)?.let { return it }
        return JSONArray()
    }

    private fun extractInfantItemObject(dataArray: JSONArray, index: Int): JSONObject? {
        val direct = dataArray.optJSONObject(index)
        if (direct != null) return direct

        val raw = dataArray.opt(index)
        if (raw is String) {
            parseJsonObjectFromString(raw)?.let { return it }
        }
        return null
    }

    private fun normalizeInfantPayload(source: JSONObject): JSONObject {
        val target = JSONObject(source.toString())

        if (!target.has("benId") || target.optLong("benId", 0L) <= 0L) {
            firstPositiveLong(
                source,
                "beneficiaryID", "beneficiaryId", "beneficiaryid", "benID", "benid",
                "motherBenId", "motherBeneficiaryID", "motherBeneficiaryId"
            )?.let { target.put("benId", it) }
        }

        if (!target.has("childBenId") || target.optLong("childBenId", 0L) < 0L) {
            firstPositiveLong(
                source,
                "childBeneficiaryID", "childBeneficiaryId", "childbeneficiaryid",
                "childBenID", "childBenId", "childID", "childId"
            )?.let { target.put("childBenId", it) }
        }

        if (!target.has("babyIndex")) {
            firstNonNegativeInt(source, "babyNo", "babyNumber", "childIndex", "index")
                ?.let { target.put("babyIndex", it) }
        }

        return target
    }

    private fun normalizeChildPayload(source: JSONObject): JSONObject {
        val target = JSONObject(source.toString())
        if (!target.has("benId") || target.optLong("benId", 0L) <= 0L) {
            firstPositiveLong(
                source,
                "beneficiaryID", "beneficiaryId", "beneficiaryid", "benID", "benid",
                "motherBenId", "motherBeneficiaryID", "motherBeneficiaryId"
            )?.let { target.put("benId", it) }
        }
        return target
    }

    private fun resolveChildRecord(
        existingRegs: List<InfantRegCache>,
        child: ChildApiPost
    ): InfantRegCache? {
        existingRegs.firstOrNull { child.id > 0L && it.id == child.id }?.let { return it }
        existingRegs.firstOrNull {
            val localName = it.babyName?.trim().orEmpty()
            val serverName = child.babyName?.trim().orEmpty()
            localName.isNotBlank() && serverName.isNotBlank() && localName.equals(serverName, ignoreCase = true)
        }?.let { return it }
        return existingRegs.firstOrNull { !it.hasRegistrationData() }
    }

    private fun mergeChildDownsync(
        existing: InfantRegCache?,
        motherPatientID: String,
        child: ChildApiPost,
        babyIndex: Int,
        userName: String
    ): InfantRegCache {
        val now = System.currentTimeMillis()
        val incoming = InfantRegCache(
            id = child.id,
            childPatientID = existing?.childPatientID,
            motherPatientID = motherPatientID,
            isActive = true,
            babyName = child.babyName ?: existing?.babyName,
            babyIndex = babyIndex,
            infantTerm = child.infantTerm ?: existing?.infantTerm,
            corticosteroidGiven = child.corticosteroidGiven ?: existing?.corticosteroidGiven,
            genderID = when (child.gender?.trim()?.lowercase(Locale.ENGLISH)) {
                "male" -> 1
                "female" -> 2
                "transgender" -> 3
                else -> existing?.genderID
            },
            babyCriedAtBirth = child.babyCriedAtBirth ?: existing?.babyCriedAtBirth,
            resuscitation = child.resuscitation ?: existing?.resuscitation,
            referred = child.referred ?: existing?.referred,
            hadBirthDefect = child.hadBirthDefect ?: existing?.hadBirthDefect,
            birthDefect = child.birthDefect ?: existing?.birthDefect,
            otherDefect = child.otherDefect ?: existing?.otherDefect,
            weight = child.weight.takeIf { it != 0.0 } ?: existing?.weight,
            breastFeedingStarted = child.breastFeedingStarted ?: existing?.breastFeedingStarted,
            opv0Dose = org.piramalswasthya.cho.network.getLongFromDate(child.opv0Dose) ?: existing?.opv0Dose,
            bcgDose = org.piramalswasthya.cho.network.getLongFromDate(child.bcgDose) ?: existing?.bcgDose,
            hepBDose = org.piramalswasthya.cho.network.getLongFromDate(child.hepBDose) ?: existing?.hepBDose,
            vitkDose = org.piramalswasthya.cho.network.getLongFromDate(child.vitkDose) ?: existing?.vitkDose,
            processed = "C",
            createdBy = child.createdBy ?: existing?.createdBy ?: userName,
            createdDate = org.piramalswasthya.cho.network.getLongFromDate(child.createdDate)
                ?: existing?.createdDate
                ?: now,
            updatedBy = child.updatedBy ?: existing?.updatedBy ?: userName,
            updatedDate = org.piramalswasthya.cho.network.getLongFromDate(child.updatedDate)
                ?: now,
            syncState = SyncState.SYNCED
        )

        return if (existing != null) {
            incoming.copy(id = existing.id)
        } else {
            incoming.copy(id = 0)
        }
    }

    private fun firstPositiveLong(source: JSONObject, vararg keys: String): Long? {
        // Direct key lookup.
        for (key in keys) {
            val value = source.optLong(key, 0L)
            if (value > 0L) return value
            source.optString(key, "").trim().toLongOrNull()?.takeIf { it > 0L }?.let { return it }
        }
        // Case-insensitive fallback.
        val lowerKeyMap = mutableMapOf<String, String>()
        val iterator = source.keys()
        while (iterator.hasNext()) {
            val realKey = iterator.next()
            lowerKeyMap[realKey.lowercase(Locale.ENGLISH)] = realKey
        }
        for (key in keys) {
            val realKey = lowerKeyMap[key.lowercase(Locale.ENGLISH)] ?: continue
            val value = source.optLong(realKey, 0L)
            if (value > 0L) return value
            source.optString(realKey, "").trim().toLongOrNull()?.takeIf { it > 0L }?.let { return it }
        }
        return null
    }

    private fun firstNonNegativeInt(source: JSONObject, vararg keys: String): Int? {
        for (key in keys) {
            if (!source.has(key)) continue
            val value = source.optInt(key, Int.MIN_VALUE)
            if (value != Int.MIN_VALUE && value >= 0) return value
            source.optString(key, "").trim().toIntOrNull()?.takeIf { it >= 0 }?.let { return it }
        }
        return null
    }

    private fun parseJsonArrayFromString(value: String?): JSONArray? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty() || !trimmed.startsWith("[")) return null
        return runCatching { JSONArray(trimmed) }.getOrNull()
    }

    private fun parseJsonObjectFromString(value: String?): JSONObject? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty() || !trimmed.startsWith("{")) return null
        return runCatching { JSONObject(trimmed) }.getOrNull()
    }

    private fun findFirstJsonArray(node: Any?): JSONArray? {
        when (node) {
            is JSONArray -> {
                if (node.length() > 0) return node
            }
            is JSONObject -> {
                val iterator = node.keys()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    val child = node.opt(key)
                    if (child is JSONArray && child.length() > 0) return child
                    if (child is JSONObject) {
                        findFirstJsonArray(child)?.let { return it }
                    } else if (child is String) {
                        parseJsonArrayFromString(child)?.let { if (it.length() > 0) return it }
                        parseJsonObjectFromString(child)?.let { obj ->
                            findFirstJsonArray(obj)?.let { return it }
                        }
                    }
                }
            }
            is String -> {
                parseJsonArrayFromString(node)?.let { if (it.length() > 0) return it }
                parseJsonObjectFromString(node)?.let { obj ->
                    findFirstJsonArray(obj)?.let { return it }
                }
            }
        }
        return null
    }

    /**
     * Ensures INFANT_REG downsync can be saved even when mother is not yet present in PATIENT.
     * Creates a lightweight local placeholder mother record keyed by beneficiaryID.
     */
    private suspend fun ensureMotherPlaceholderForInfant(
        benId: Long
    ): Patient? {
        if (benId <= 0L) return null
        val existing = patientDao.getPatientByAnyBeneficiaryId(benId)
        if (existing != null) return existing

        val placeholderPatientId = "infant-mother-$benId"
        val now = Date()
        val placeholder = Patient(
            patientID = placeholderPatientId,
            firstName = "Mother",
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
            patientDao.getPatient(placeholderPatientId)
        }.onFailure { err ->
            Timber.w(err, "Unable to create mother placeholder for infant benId=$benId")
        }.getOrNull()
    }
}
