package org.piramalswasthya.cho.ui.commons.lab_technician

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.ProcedureRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LabTechnicianFormViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    savedStateHandle: SavedStateHandle,
    private val userRepo: UserRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val benFlowRepo: BenFlowRepo,
    private val patientRepo: PatientRepo,
    private val procedureRepo: ProcedureRepo,
    private val caseRecordeRepo: CaseRecordeRepo
) : ViewModel() {

    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser

    private var _isDataSaved = MutableLiveData(false)
    val isDataSaved: LiveData<Boolean>
        get() = _isDataSaved

    private var _boolCall = MutableLiveData(false)

    private val _cacheSaved = MutableLiveData(false)
    val cacheSaved: LiveData<Boolean>
        get() = _cacheSaved

    val boolCall: LiveData<Boolean>
        get() = _boolCall

    private var _procedures = MutableLiveData<List<ProcedureDTO>>(null)
    val procedures: LiveData<List<ProcedureDTO>>
        get() = _procedures

    private val _benFlows = MutableLiveData<List<org.piramalswasthya.cho.model.BenFlow>?>()
    val benFlows: LiveData<List<org.piramalswasthya.cho.model.BenFlow>?> = _benFlows

    @SuppressLint("StaticFieldLeak")
    val context: Context = application.applicationContext

    val state = savedStateHandle

    val isEntitySaved = MutableLiveData<Boolean>()

    /**
     * Returns the set of procedure IDs (master IDs) that the doctor selected for this visit.
     * Uses newTestIds (current selection) or previousTestIds (synced from server).
     */
    private suspend fun getDoctorSelectedTestIds(patientID: String, benVisitNo: Int): Set<Long> {
        val investigation = caseRecordeRepo.getInvestigationCaseRecordByPatientIDAndBenVisitNo(
            patientID,
            benVisitNo
        )?.investigationCaseRecord ?: return emptySet()
        val idsStr = investigation.newTestIds?.takeIf { it.isNotBlank() }
            ?: investigation.previousTestIds?.takeIf { it.isNotBlank() } ?: return emptySet()
        return idsStr.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
    }

    /**
     * Ensures lab procedures are in DB for this visit so form can render from local data (no API).
     * First ensures procedure_master has seed data (in case migration didn't run on fresh install).
     * If doctor already saved investigation with newTestIds, procedures were copied when saving.
     * If procedures are missing, copy from ProcedureMaster using newTestIds or previousTestIds (synced data).
     * Then shows only the test forms that the doctor selected (filters by newTestIds/previousTestIds).
     */
    suspend fun downloadProcedure(benVisitInfo: PatientDisplayWithVisitInfo) {
        withContext(Dispatchers.IO) {
            procedureRepo.ensureLabProcedureMasterSeed()
            var procedures = patientRepo.getProcedures(benVisitInfo)
            if (procedures.isNullOrEmpty()) {
                val investigation = caseRecordeRepo.getInvestigationCaseRecordByPatientIDAndBenVisitNo(
                    benVisitInfo.patient.patientID,
                    benVisitInfo.benVisitNo!!
                )?.investigationCaseRecord
                // Use newTestIds (doctor-selected) or previousTestIds (synced from server)
                val idsToCopy = investigation?.newTestIds?.takeIf { it.isNotBlank() }
                    ?: investigation?.previousTestIds?.takeIf { it.isNotBlank() }
                idsToCopy?.let {
                    procedureRepo.copyProceduresFromMasterForVisit(
                        benVisitInfo.patient.patientID,
                        benVisitInfo.benVisitNo!!,
                        it
                    )
                    procedures = patientRepo.getProcedures(benVisitInfo)
                }
            }
            val selectedIds = getDoctorSelectedTestIds(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
            val filtered = if (selectedIds.isEmpty()) procedures
            else procedures?.filter { it.procedureID in selectedIds } ?: emptyList()
            _procedures.postValue(filtered)
        }
    }

    /**
     * Fetches procedures for this visit and shows only the test forms that the doctor selected.
     */
    suspend fun getPrescribedProcedures(benVisitInfo: PatientDisplayWithVisitInfo) {
        withContext(Dispatchers.IO) {
            val procedures = patientRepo.getProcedures(benVisitInfo)
            val selectedIds = getDoctorSelectedTestIds(benVisitInfo.patient.patientID, benVisitNo = benVisitInfo.benVisitNo!!)
            val filtered = if (selectedIds.isEmpty()) procedures
            else procedures?.filter { it.procedureID in selectedIds } ?: emptyList()
            _procedures.postValue(filtered)
            Timber.d("fetched procedures")
        }
    }

    fun saveEntity() {
        viewModelScope.launch {
            //map entity
            isEntitySaved.value = true
        }
    }

    fun getLoggedInUserDetails() {
        viewModelScope.launch {
            try {
                _loggedInUser = userRepo.getUserCacheDetails()
                _boolCall.value = true
            } catch (e: Exception) {
                Timber.d("Error in calling getLoggedInUserDetails() $e")
                _boolCall.value = false
            }
        }
    }

    fun resetBool() {
        _boolCall.value = false
    }

    fun getVisitReasonByBenFlowID(beneficiaryID: Long) {
        viewModelScope.launch {
            try {
                val benFlowList = benFlowRepo.getBenFlowByBenFlowID(beneficiaryID)
                _benFlows.value = benFlowList
            } catch (e: Exception) {
                _benFlows.value = emptyList()
            }
        }
    }

    fun saveLabData(dtos: List<ProcedureDTO>?, benVisitInfo: PatientDisplayWithVisitInfo) {
        try {

            viewModelScope.launch {

                dtos?.forEach { procedureDTO ->

                    val procedure =
                        patientRepo.getProcedure(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!, procedureDTO.procedureID)
                    procedureDTO.compListDetails.forEach { componentDetailDTO ->
                        Timber.d("mjf" + componentDetailDTO.testComponentName + ":" + componentDetailDTO.testResultValue)
                        var componentDetails = patientRepo.getComponent(procedure.id, componentDetailDTO.testComponentID)
                        componentDetails.testResultValue = componentDetailDTO.testResultValue
                        componentDetails.remarks = componentDetailDTO.remarks
                        patientRepo.updateComponentDetails(componentDetails)
                    }

                }

                withContext(Dispatchers.IO) {
                    procedureRepo.syncLabResultsToDownsyncTable(
                        benVisitInfo.patient.patientID,
                        benVisitInfo.benVisitNo!!
                    )
                }

                // update sync state for lab data
                val patientVisitInfoSync = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
                    benVisitInfo.patient.patientID,
                    benVisitInfo.benVisitNo!!
                )!!
                patientVisitInfoSync.labDataSynced = SyncState.UNSYNCED
                patientVisitInfoSync.doctorFlag = 3

                patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
                WorkerUtils.labPushWorker(context)

                _isDataSaved.value = true

                withContext(Dispatchers.IO) {
                    _cacheSaved.postValue(true)
                }

                _isDataSaved.value = true

            }

        } catch (e: Exception) {
            Timber.d("error saving lab records due to $e")
        }
    }
}