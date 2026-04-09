package org.piramalswasthya.cho.repositories

import android.icu.util.Calendar
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
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
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.network.AmritApiService
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
//import org.piramalswasthya.sakhi.helpers.getTodayMillis
//import org.piramalswasthya.sakhi.model.*
//import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
//import org.piramalswasthya.sakhi.network.getLongFromDate
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MaternalHealthRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val maternalHealthDao: MaternalHealthDao,
    private val ashaDueListDao: AshaDueListDao,
    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao,
//    private val preferenceDao: PreferenceDao,
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
