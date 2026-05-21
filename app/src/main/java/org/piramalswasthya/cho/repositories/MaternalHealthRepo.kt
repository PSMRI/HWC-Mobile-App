package org.piramalswasthya.cho.repositories

import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.AshaDueListCache
//import org.piramalswasthya.cho.database.room.InAppDb
//import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.AbortionDomain
import org.piramalswasthya.cho.model.PatientWithPwrCache
import org.piramalswasthya.cho.model.PmsmaDomain
import org.piramalswasthya.cho.model.asPmsmaDomainModel
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.getLongFromDate
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.AshaDueListDao
import org.piramalswasthya.cho.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
//import org.piramalswasthya.sakhi.database.room.dao.BenDao
//import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.helpers.getTodayMillis
import org.piramalswasthya.cho.helpers.getWeeksOfPregnancy
import org.piramalswasthya.cho.model.ANCPost
import org.piramalswasthya.cho.model.AncCompletedListItem
import org.piramalswasthya.cho.model.AncDueListItem
import org.piramalswasthya.cho.model.PwrPost
//import org.piramalswasthya.sakhi.helpers.getTodayMillis
//import org.piramalswasthya.sakhi.model.*
//import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
//import org.piramalswasthya.sakhi.network.getLongFromDate
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import retrofit2.Response

/** Outcome of an upload attempt for one record. */
private sealed class AncUploadResult {
    object Success : AncUploadResult()
    /** Recoverable failure (network blip, 5xx, token refresh): worth retrying next cycle. */
    object Transient : AncUploadResult()
    /** Server rejected the payload itself (e.g. statusCode 5000 "Saving anc data to db failed").
     *  Retrying the same body forever just wastes cycles — caller should park the row. */
    data class Terminal(val message: String) : AncUploadResult()
}

class MaternalHealthRepo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val amritApiService: AmritApiService,
    private val maternalHealthDao: MaternalHealthDao,
    private val ashaDueListDao: AshaDueListDao,
    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao,
    private val preferenceDao: PreferenceDao,
) {

    suspend fun getSavedRegistrationRecord(benId: String): PregnantWomanRegistrationCache? {
        return maternalHealthDao.getSavedRecord(benId)
    }



    suspend fun getActiveRegistrationRecord(benId: String): PregnantWomanRegistrationCache? {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getSavedActiveRecord(benId)
        }
    }

    suspend fun getLastVisitNumber(benId: String): Int? {
        return maternalHealthDao.getLastVisitNumber(benId)
    }

    suspend fun getSavedAncRecord(benId: String, visitNumber: Int): PregnantWomanAncCache? {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getSavedRecord(benId, visitNumber)
        }
    }

    suspend fun getAllActiveAncRecords(benId: String): List<PregnantWomanAncCache> {
         return maternalHealthDao.getAllActiveAncRecords(benId)
    }

    suspend fun getCompletedActiveAncRecords(benId: String): List<PregnantWomanAncCache> {
        return maternalHealthDao.getCompletedActiveAncRecords(benId)
    }

    suspend fun getLastAnc(benId: String): PregnantWomanAncCache? {
        return maternalHealthDao.getLastAnc(benId)
    }

    suspend fun getLastCompletedAnc(benId: String): PregnantWomanAncCache? {
        return maternalHealthDao.getLastCompletedAnc(benId)
    }

    suspend fun persistRegisterRecord(pregnancyRegistrationForm: PregnantWomanRegistrationCache) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.saveRecord(pregnancyRegistrationForm)
            generateAndPersistAncSchedule(pregnancyRegistrationForm)
        }
    }


    suspend fun registerPregnancyWithAncAndAshaDueList(
        pwr: PregnantWomanRegistrationCache,
        benId: String,
        ashaId: Int
    ): Long = withContext(Dispatchers.IO) {
        val registrationId = maternalHealthDao.saveRecord(pwr)
        generateAndPersistAncSchedule(pwr)
        addBeneficiaryToAshaDueList(benId, ashaId, pwr.createdBy)
        registrationId
    }

    private suspend fun addBeneficiaryToAshaDueList(patientID: String, ashaId: Int, createdBy: String) {
        val patient = patientDao.getPatient(patientID)
        val beneficiaryID = patient?.beneficiaryID
        val record = AshaDueListCache(
            patientID = patientID,
            beneficiaryID = beneficiaryID,
            listType = "ANC",
            addedDate = System.currentTimeMillis(),
            ashaId = ashaId,
            createdBy = createdBy,
            syncState = SyncState.UNSYNCED
        )
        ashaDueListDao.insert(record)
    }


    private suspend fun generateAndPersistAncSchedule(pwr: PregnantWomanRegistrationCache) {
        if (maternalHealthDao.getLastActiveVisitNumber(pwr.patientID) != null) return
        val cal = Calendar.getInstance().apply {
            timeInMillis = pwr.lmpDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lmpStartOfDayMillis = cal.timeInMillis
        val ancScheduleDaysFromLmp = listOf(84,98,196,252)
        ancScheduleDaysFromLmp.forEachIndexed { index, days ->
            val scheduledDateMillis = lmpStartOfDayMillis + TimeUnit.DAYS.toMillis(days.toLong())
            val ancCache = PregnantWomanAncCache(
                patientID = pwr.patientID,
                visitNumber = index + 1,
                ancDate = scheduledDateMillis,
                isActive = true,
                createdBy = pwr.createdBy,
                updatedBy = pwr.updatedBy,
                syncState = SyncState.UNSYNCED
            )
            maternalHealthDao.saveRecord(ancCache)
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

    suspend fun updatePwr(pwr: PregnantWomanRegistrationCache) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.updatePwr(pwr)
        }
    }

    suspend fun getAncDueList(ancStage: Int): List<AncDueListItem> {
        return withContext(Dispatchers.IO) {
            val allPwr = maternalHealthDao.getAllActivePregnancyRegistrations()
            val todayMillis = getTodayMillis()
            allPwr.mapNotNull { pwr ->
                val ancRecords = maternalHealthDao.getAllActiveAncRecords(pwr.patientID)
                
                // Skip if already delivered
                if (ancRecords.any { it.pregnantWomanDelivered == true }) {
                    return@mapNotNull null
                }
                
                val gaWeeks = getWeeksOfPregnancy(todayMillis, pwr.lmpDate)
                val isDue = isAncStageDue(ancStage, ancRecords, gaWeeks)
                
                if (isDue) {
                    Timber.d("ANC Due List: Patient ${pwr.patientID} added to ANC $ancStage due list (GA: $gaWeeks weeks)")
                    AncDueListItem(pwr.patientID, gaWeeks, ancStage)
                } else null
            }
        }
    }

    private fun isAncStageDue(ancStage: Int, ancRecords: List<PregnantWomanAncCache>, gaWeeks: Int): Boolean {
        // ANC 1-4: previous stage completed (weight IS NOT NULL) + GA in range
        // Per Jira MHWC-196: Check if previous visit is COMPLETED, not just exists
        return when (ancStage) {
            1 -> {
                // ANC 1: Pregnancy registered AND GA ≤12 weeks (≤84 days from LMP)
                val anc1Completed = ancRecords.any { it.visitNumber == 1 && it.weight != null }
                !anc1Completed && gaWeeks <= Konstants.maxAnc1Week
            }
            2 -> {
                // ANC 2: ANC 1 completed AND GA ≥14 weeks AND <28 weeks
                val anc1Completed = ancRecords.any { it.visitNumber == 1 && it.weight != null }
                val anc2Completed = ancRecords.any { it.visitNumber == 2 && it.weight != null }
                anc1Completed && !anc2Completed && gaWeeks >= Konstants.minAnc2Week && gaWeeks < 28
            }
            3 -> {
                // ANC 3: ANC 2 completed AND GA ≥28 weeks AND <36 weeks
                val anc2Completed = ancRecords.any { it.visitNumber == 2 && it.weight != null }
                val anc3Completed = ancRecords.any { it.visitNumber == 3 && it.weight != null }
                anc2Completed && !anc3Completed && gaWeeks >= Konstants.minAnc3Week && gaWeeks < 36
            }
            4 -> {
                // ANC 4: ANC 3 completed AND GA ≥36 weeks AND ≤40 weeks
                val anc3Completed = ancRecords.any { it.visitNumber == 3 && it.weight != null }
                val anc4Completed = ancRecords.any { it.visitNumber == 4 && it.weight != null }
                anc3Completed && !anc4Completed && gaWeeks >= Konstants.minAnc4Week && gaWeeks <= Konstants.maxAnc4Week
            }
            else -> false
        }
    }

    suspend fun getAncCompletedList(ancStage: Int): List<AncCompletedListItem> {
        return withContext(Dispatchers.IO) {
            val allPwr = maternalHealthDao.getAllActivePregnancyRegistrations()
            allPwr.mapNotNull { pwr ->
                val ancRecords = maternalHealthDao.getAllActiveAncRecords(pwr.patientID)
                if (ancRecords.any { it.pregnantWomanDelivered == true }) return@mapNotNull null
                // Per Jira MHWC-196: Only include visits that are COMPLETED (weight IS NOT NULL)
                ancRecords.find { it.visitNumber == ancStage && it.weight != null }?.let { anc ->
                    Timber.d("ANC Completed List: Patient ${pwr.patientID} in ANC $ancStage completed list (Visit: ${anc.visitNumber})")
                    AncCompletedListItem(pwr.patientID, ancStage, anc.visitNumber)
                }
            }
        }
    }

    suspend fun processNewAncVisit(): Boolean {
        return withContext(Dispatchers.IO) {
            val ancList = maternalHealthDao.getAllUnprocessedAncVisits()
            Timber.d("ANC upload queue size: ${ancList.size}")
            if (ancList.isEmpty()) {
                Timber.d("No unsynced ANC records found for upload")
                return@withContext true
            }

            val ancPostList = mutableSetOf<ANCPost>()
            val user = userRepo.getLoggedInUser()
            var hasFailures = false
            ancList.forEach {
                ancPostList.clear()
                val ben = patientDao.getPatient(it.patientID)
                if(ben.beneficiaryID != null){
                    ancPostList.add(
                        it.asPostModel(
                            benId = ben.beneficiaryID!!,
                            benRegId = ben.beneficiaryRegID,
                            providerServiceMapID = user?.serviceMapId,
                            context = context,
                        )
                    )
                    it.syncState = SyncState.SYNCING
                    maternalHealthDao.updateANC(it)
                    Timber.d("processNewAncVisit: posting row id=${it.id} patientID=${it.patientID}")
                    when (val result = postDataToAmritServer(ancPostList)) {
                        AncUploadResult.Success -> {
                            // Targeted column update — avoids writing the whole
                            // row (which would persist any stale field still on
                            // `it`) and is robust against parallel writers in
                            // the same sync chain.
                            maternalHealthDao.markAncSynced(it.id)
                            Timber.d("processNewAncVisit: row id=${it.id} marked SYNCED")
                        }
                        is AncUploadResult.Terminal -> {
                            // Park the row: getAllUnprocessedAncVisits filters on
                            // processed IN ('N','U'), so "F" stops the retry loop.
                            // A subsequent edit resets processed to 'U' and the row
                            // re-enters the queue.
                            Timber.w("Parking ANC row ${it.id}: ${result.message}")
                            it.processed = "F"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthDao.updateANC(it)
                            hasFailures = true
                        }
                        AncUploadResult.Transient -> {
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthDao.updateANC(it)
                            hasFailures = true
                        }
                    }
                } else {
                    Timber.w("Skipping ANC upload for patient ${it.patientID}: beneficiaryID is null")
                    hasFailures = true
                }
            }

            return@withContext !hasFailures
        }
    }

    suspend fun processNewPWRRecords(): Boolean {
        return withContext(Dispatchers.IO) {
            val pwrList = maternalHealthDao.getAllUnprocessedPWRs()
            if (pwrList.isEmpty()) {
                Timber.d("No unsynced PWR records found for upload")
                return@withContext true
            }

            var hasFailures = false
            val pwrPostList = mutableSetOf<PwrPost>()

            pwrList.forEach { pwr ->
                pwrPostList.clear()
                val patient = patientDao.getPatient(pwr.patientID)
                val benId = patient.beneficiaryID
                if (benId == null) {
                    Timber.w("Skipping PWR upload for patient ${pwr.patientID}: beneficiaryID is null")
                    hasFailures = true
                    return@forEach
                }

                pwr.syncState = SyncState.SYNCING
                maternalHealthDao.updatePwr(pwr)

                pwrPostList.add(pwr.asPwrPost().copy(benId = benId))
                val uploadDone = postPwrDataToAmritServer(pwrPostList)
                if (uploadDone) {
                    pwr.processed = "P"
                    pwr.syncState = SyncState.SYNCED
                } else {
                    pwr.syncState = SyncState.UNSYNCED
                    hasFailures = true
                }
                maternalHealthDao.updatePwr(pwr)
            }

            return@withContext !hasFailures
        }
    }

    /**
     * Get all patients with their pregnancy registration data
     */
    fun getAllPatientsWithPWR(): Flow<List<PatientWithPwrCache>> {
        return maternalHealthDao.getAllPatientsWithPWR()
            .combine(maternalHealthDao.getAllPatientsWithPWRFromEligibleCoupleTracking()) { pwrList, ectPositiveList ->
                (pwrList + ectPositiveList)
                    .distinctBy { it.patient.patientID }
                    .sortedByDescending {
                        it.getActiveOrLatestPwr()?.createdDate ?: it.patient.registrationDate?.time ?: 0L
                    }
            }
    }

    fun getAllPatientsWithANC(): Flow<List<PatientWithPwrCache>> {
        return maternalHealthDao.getAllPatientsWithANC()
            .combine(maternalHealthDao.getAllPatientsWithPWRFromEligibleCoupleTracking()) { pwrList, ectPositiveList ->
                (pwrList + ectPositiveList)
                    .distinctBy { it.patient.patientID }
                    .sortedByDescending {
                        it.getActiveOrLatestPwr()?.createdDate ?: it.patient.registrationDate?.time ?: 0L
                    }
            }
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
     * Get count of women on the Pregnant Women Registration list.
     * Derives from the same combined Flow as PregnantWomenRegistrationFragment
     * (PWR-side + ECT-positive catch-all) and applies the same Kotlin predicate,
     * so the grid count and the list row count cannot drift.
     */
    fun getPWRCount(): Flow<Int> {
        return getAllPatientsWithPWR().map { list ->
            list.map { it.asDomainModel() }
                .count { domain ->
                    val isFemale = domain.patient.genderID == 2
                    val age = domain.patient.age ?: 0
                    val isReproductiveAge = age in 15..49
                    val isPostnatal = domain.patient.statusOfWomanID == 3
                    isFemale && isReproductiveAge && !isPostnatal
                }
        }
    }

    /**
     * Get count of women eligible for ANC.
     * Derives from the same combined Flow as ANCVisitsFragment (PWR-side + ECT-positive catch-all)
     * and applies the same Kotlin predicate, so the grid count and the list row count cannot drift.
     */
    fun getANCCount(): Flow<Int> {
        val ancEligibilityWindowMillis = 35L * 24 * 60 * 60 * 1000
        return getAllPatientsWithANC().map { list ->
            val now = System.currentTimeMillis()
            val cutoff = now - ancEligibilityWindowMillis
            list.map { it.asDomainModel() }
                .count { domain ->
                    val hasActivePWR = domain.pwr != null && domain.isActive()
                    val isFemale = domain.patient.genderID == 2
                    val age = domain.patient.age ?: 0
                    val isReproductiveAge = age in 15..49
                    val isEligibleForANC = domain.pwr?.lmpDate?.let { lmp ->
                        lmp <= cutoff
                    } ?: false
                    hasActivePWR && isFemale && isReproductiveAge && isEligibleForANC
                }
        }
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

    /**
     * Get e-PMSMA women — the subset of ANC-eligible women who have at least one
     * active ANC visit flagged anyHighRisk = true.
     *
     * Derived from the same Flow as ANCVisitsFragment / getANCCount and intersected
     * with the high-risk patientID set, so the e-PMSMA list cannot drift from the
     * ANC tile.
     */
    fun getEPmsmaWomenList(): Flow<List<PmsmaDomain>> {
        val ancEligibilityWindowMillis = 35L * 24 * 60 * 60 * 1000
        return getAllPatientsWithANC()
            .combine(maternalHealthDao.getHighRiskAncPatientIDs()) { patientList, hrIds ->
                val cutoff = System.currentTimeMillis() - ancEligibilityWindowMillis
                val hrSet = hrIds.toHashSet()
                patientList
                    .filter { hrSet.contains(it.patient.patientID) }
                    .filter { p ->
                        val domain = p.asDomainModel()
                        val hasActivePWR = domain.pwr != null && domain.isActive()
                        val isFemale = domain.patient.genderID == 2
                        val age = domain.patient.age ?: 0
                        val isReproductiveAge = age in 15..49
                        val isEligibleForANC =
                            domain.pwr?.lmpDate?.let { lmp -> lmp <= cutoff } ?: false
                        hasActivePWR && isFemale && isReproductiveAge && isEligibleForANC
                    }
                    .map { it.asPmsmaDomainModel() }
                    .sortedByDescending { it.pwr?.dateOfRegistration ?: 0L }
            }
    }

    /**
     * Count of e-PMSMA women. Derived from getEPmsmaWomenList so the tile count
     * and list row count cannot drift.
     */
    fun getEPmsmaWomenCount(): Flow<Int> = getEPmsmaWomenList().map { it.size }

    /**
     * Get all women eligible for neonatal outcome (those with a saved delivery outcome)
     */
    fun getNeonatalOutcomeEligibleWomen(): Flow<List<PatientWithPwrCache>> {
        return maternalHealthDao.getNeonatalOutcomeEligibleWomenPatientIDs()
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
     * Get patientIDs of women who have a saved delivery outcome
     */
    fun getNeonatalOutcomeEligibleWomenPatientIDs(): Flow<List<String>> {
        return maternalHealthDao.getNeonatalOutcomeEligibleWomenPatientIDs()
    }

    /**
     * Get count of women eligible for neonatal outcome
     */
    fun getNeonatalOutcomeEligibleWomenCount(): Flow<Int> {
        return maternalHealthDao.getNeonatalOutcomeEligibleWomenCount()
    }

    private suspend fun postDataToAmritServer(ancPostList: MutableSet<ANCPost>): AncUploadResult {
        if (ancPostList.isEmpty()) return AncUploadResult.Transient
        val user =
            userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

        // Bounded retry: recursion + non-local control flow via thrown
        // SocketTimeoutException could spin forever (and eventually overflow
        // the stack) when the server is persistently issuing 5002 or the
        // network is permanently flaky. After MAX_ANC_UPLOAD_ATTEMPTS we
        // yield to WorkManager — its outer backoff is the right place to
        // delay further attempts.
        repeat(MAX_ANC_UPLOAD_ATTEMPTS) { attempt ->
            try {
                val response = amritApiService.postAncForm(ancPostList.toList())
                val httpCode = response.code()
                if (httpCode != 200) {
                    Timber.w("ANC saveAll bad HTTP code=$httpCode (attempt ${attempt + 1})")
                    return AncUploadResult.Transient
                }

                val responseString = response.body()?.string()
                Timber.d("ANC saveAll response body: ${responseString?.take(300)}")
                if (responseString.isNullOrBlank()) {
                    Timber.d("ANC saveAll succeeded with empty response body")
                    return AncUploadResult.Success
                }

                val jsonObj = try {
                    JSONObject(responseString)
                } catch (e: Exception) {
                    Timber.w(e, "ANC saveAll non-JSON body, treating as success: $responseString")
                    return AncUploadResult.Success
                }

                val statusCode = jsonObj.optInt("statusCode", 200)
                val errorMessage = jsonObj.optString("errorMessage")

                when (statusCode) {
                    200 -> {
                        Timber.d("ANC saved successfully to server")
                        return AncUploadResult.Success
                    }
                    5002 -> {
                        Timber.d("ANC saveAll got 5002; refreshing token (attempt ${attempt + 1})")
                        if (!userRepo.refreshTokenTmc(user.userName, user.password)) {
                            Timber.w("Token refresh failed")
                            return AncUploadResult.Transient
                        }
                        // Token refreshed — fall through to next loop iteration.
                        return@repeat
                    }
                    // 5000 = server-side persistence failure — usually a payload
                    // the server refuses to accept (e.g. oversized base64 image).
                    // Retrying the same body just loops forever, so we mark it
                    // terminal and park the row until the user edits / re-saves.
                    5000 -> {
                        Log.d("anc error message", errorMessage)
                        Timber.w("ANC saveAll permanently rejected (5000): $errorMessage")
                        return AncUploadResult.Terminal(errorMessage)
                    }
                    else -> {
                        Log.d("anc error message", errorMessage)
                        Timber.w("ANC saveAll server error $statusCode: $errorMessage")
                        return AncUploadResult.Transient
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("ANC saveAll timed out (attempt ${attempt + 1}); retrying")
                // Fall through to next iteration.
            } catch (e: JSONException) {
                Timber.d("ANC saveAll JSON parse error: $e")
                return AncUploadResult.Transient
            }
        }

        Timber.w("ANC saveAll exhausted $MAX_ANC_UPLOAD_ATTEMPTS attempts; deferring to next sync cycle")
        return AncUploadResult.Transient
    }

    private companion object {
        private const val MAX_ANC_UPLOAD_ATTEMPTS = 3
    }

    private suspend fun postPwrDataToAmritServer(pwrPostList: MutableSet<PwrPost>): Boolean {
        if (pwrPostList.isEmpty()) return false
        val user = userRepo.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in!!")
        try {
            val response = amritApiService.postPregnantWomanForm(pwrPostList.toList())
            val statusCode = response.code()
            if (statusCode == 200) {
                val responseString = response.body()?.string()
                if (responseString != null) {
                    val jsonObj = JSONObject(responseString)
                    val responseStatusCode = jsonObj.optInt("statusCode", 200)
                    val errorMessage = jsonObj.optString("errorMessage")
                    when (responseStatusCode) {
                        200 -> {
                            Timber.d("PWR saved successfully to server")
                            return true
                        }
                        5002 -> {
                            if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                throw SocketTimeoutException()
                            }
                        }
                        else -> {
                            Timber.w("PWR saveAll failed: $errorMessage")
                            throw IOException("PWR saveAll failed with statusCode=$responseStatusCode")
                        }
                    }
                }
            }
            Timber.w("Bad response from server for PWR saveAll: $response")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught timeout for PWR sync $e; retrying")
            return postPwrDataToAmritServer(pwrPostList)
        } catch (e: JSONException) {
            Timber.d("Caught JSON exception for PWR sync $e")
            return false
        }
    }

    private fun extractGetAllDataArray(jsonObj: JSONObject): JSONArray {
        return when (val dataNode = jsonObj.opt("data")) {
            is JSONArray -> dataNode
            is JSONObject -> dataNode.optJSONArray("data")
            else -> null
        } ?: JSONArray()
    }

    private suspend fun <T> pullMaternalRecordsFromServer(
        moduleName: String,
        fetch: suspend (VillageIdList) -> Response<ResponseBody>,
        retryOnTimeout: suspend () -> Boolean,
        parseItem: (Gson, JSONObject) -> T,
        benIdOf: (T) -> Long,
        saveItem: suspend (item: T, patientID: String, userName: String) -> Unit
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    RepositorySyncUtils.parseVillageIds(user.assignVillageIds ?: ""),
                    preferenceDao.getLastPatientSyncTime()
                )
                val response = fetch(villageList)
                if (response.code() != 200) {
                    Timber.w("Bad response from server for $moduleName getAll: $response")
                    return@withContext false
                }

                val responseString = response.body()?.string()
                if (responseString.isNullOrBlank()) {
                    Timber.w("Empty response body for $moduleName getAll")
                    return@withContext false
                }

                val jsonObj = JSONObject(responseString)
                val responseStatusCode = jsonObj.optInt("statusCode", 200)
                val errorMessage = jsonObj.optString("errorMessage")
                when (responseStatusCode) {
                    200 -> {
                        val dataArray = extractGetAllDataArray(jsonObj)
                        val gson = Gson()
                        var savedCount = 0
                        for (i in 0 until dataArray.length()) {
                            val item = parseItem(gson, dataArray.getJSONObject(i))
                            val benId = benIdOf(item)
                            if (benId == 0L) {
                                Timber.w("Skipping $moduleName getAll item with invalid benId at index=$i")
                                continue
                            }

                            val patient = patientDao.getPatientByAnyBeneficiaryId(benId)
                            if (patient == null) {
                                Timber.w("No local patient found for $moduleName benId=$benId, skipping")
                                continue
                            }

                            saveItem(item, patient.patientID, user.userName)
                            savedCount++
                        }
                        Timber.d("$moduleName getAll downsync completed, saved=$savedCount received=${dataArray.length()}")
                        return@withContext true
                    }

                    5000 -> {
                        Timber.d("No $moduleName records found on server")
                        return@withContext true
                    }

                    5002 -> {
                        if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                            throw SocketTimeoutException()
                        }
                    }

                    else -> {
                        Timber.w("$moduleName getAll failed: $errorMessage")
                        throw IOException("$moduleName getAll failed with statusCode=$responseStatusCode")
                    }
                }
                return@withContext false
            } catch (e: SocketTimeoutException) {
                Timber.d("Caught timeout for $moduleName getAll $e; retrying")
                return@withContext retryOnTimeout()
            } catch (e: JSONException) {
                Timber.d("Caught JSON exception for $moduleName getAll $e")
                return@withContext false
            }
        }
    }

    suspend fun pullPregnantWomenFromServer(): Boolean {
        return pullMaternalRecordsFromServer(
            moduleName = "PWR",
            fetch = { villageList -> amritApiService.getAllPregnantWomen(villageList) },
            retryOnTimeout = { pullPregnantWomenFromServer() },
            parseItem = { gson, json -> gson.fromJson(json.toString(), PwrPost::class.java) },
            benIdOf = { pwrNetwork -> pwrNetwork.benId },
            saveItem = { pwrNetwork, patientID, userName ->
                val incoming = pwrNetwork.toPwrCache().copy(
                    patientID = patientID,
                    processed = "P",
                    syncState = SyncState.SYNCED,
                    updatedDate = System.currentTimeMillis(),
                    updatedBy = pwrNetwork.updatedBy.ifBlank { userName }
                )
                val existing = maternalHealthDao.getSavedRecord(patientID)
                val merged = if (existing != null) {
                    incoming.copy(
                        id = existing.id,
                        createdDate = if (existing.createdDate > 0L) existing.createdDate else incoming.createdDate,
                        createdBy = if (existing.createdBy.isNotBlank()) existing.createdBy else incoming.createdBy
                    )
                } else {
                    incoming
                }
                maternalHealthDao.saveRecord(merged)
            }
        )
    }

    suspend fun pullAncVisitsFromServer(): Boolean {
        return pullMaternalRecordsFromServer(
            moduleName = "ANC",
            fetch = { villageList -> amritApiService.getAllAncVisits(villageList) },
            retryOnTimeout = { pullAncVisitsFromServer() },
            parseItem = { gson, json -> gson.fromJson(json.toString(), ANCPost::class.java) },
            benIdOf = { ancNetwork -> ancNetwork.benId },
            saveItem = { ancNetwork, patientID, userName ->
                val incoming = ancNetwork.toAncCache().copy(
                    patientID = patientID,
                    processed = "P",
                    syncState = SyncState.SYNCED,
                    updatedDate = if (ancNetwork.updatedDate.isNullOrBlank()) System.currentTimeMillis() else getLongFromDate(ancNetwork.updatedDate),
                    updatedBy = ancNetwork.updatedBy.ifBlank { userName }
                )
                val existing = maternalHealthDao.getSavedRecord(patientID, incoming.visitNumber)
                val merged = if (existing != null) {
                    incoming.copy(
                        id = existing.id,
                        createdDate = if (existing.createdDate > 0L) existing.createdDate else incoming.createdDate,
                        createdBy = if (existing.createdBy.isNotBlank()) existing.createdBy else incoming.createdBy
                    )
                } else {
                    incoming
                }
                maternalHealthDao.saveRecord(merged)
            }
        )
    }
}
