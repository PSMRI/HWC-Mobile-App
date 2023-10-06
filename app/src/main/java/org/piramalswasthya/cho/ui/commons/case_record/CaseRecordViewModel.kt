package org.piramalswasthya.cho.ui.commons.case_record

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import timber.log.Timber
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class CaseRecordViewModel @Inject constructor(
    private val caseRecordeRepo: CaseRecordeRepo,
    private val maleMasterDataRepository: MaleMasterDataRepository,
    private val doctorMasterDataMaleRepo: DoctorMasterDataMaleRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vitalsRepo: VitalsRepo,
    private val visitRepo: VisitReasonsAndCategoriesRepo,
    private val patientRepo: PatientRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo

    ): ViewModel() {

    private val _isClickedSS=MutableLiveData<Boolean>(false)

    val isClickedSS: MutableLiveData<Boolean>
        get() = _isClickedSS

    private val _diagnosisVal=MutableLiveData<Boolean>(false)
    val diagnosisVal: MutableLiveData<Boolean>
        get() = _diagnosisVal

    private var _formMedicineDosage: LiveData<List<ItemMasterList>>
    val formMedicineDosage: LiveData<List<ItemMasterList>>
        get() = _formMedicineDosage

    private var _counsellingProvided: LiveData<List<CounsellingProvided>>
    val counsellingProvided: LiveData<List<CounsellingProvided>>
        get() = _counsellingProvided

    private var _procedureDropdown: LiveData<List<ProceduresMasterData>>
    val procedureDropdown: LiveData<List<ProceduresMasterData>>
        get() = _procedureDropdown

    private var _higherHealthCare: LiveData<List<HigherHealthCenter>>
    val higherHealthCare: LiveData<List<HigherHealthCenter>>
        get() = _higherHealthCare

    private val _chiefComplaintDB = MutableLiveData<List<ChiefComplaintDB>>()
    val chiefComplaintDB: LiveData<List<ChiefComplaintDB>>
        get() = _chiefComplaintDB

    private val _vitalsDB = MutableLiveData<PatientVitalsModel>()
    val vitalsDB: LiveData<PatientVitalsModel>
        get() = _vitalsDB

    init {
        _counsellingProvided = MutableLiveData()
        getCounsellingTypes()
        _formMedicineDosage = MutableLiveData()
        getFormMaster()
        _procedureDropdown = MutableLiveData()
        getProcedureDropdown()
        _higherHealthCare = MutableLiveData()
        getHigherHealthCareDropdown()

    }
      fun getVitalsDB(patientID:String) {
        viewModelScope.launch {
            try {
                _vitalsDB.value =
                    vitalsRepo.getVitalsDetailsByPatientID(patientID)

            } catch (e: java.lang.Exception) {
                Timber.d("Error in Getting Higher Health Care $e")
            }
        }
    }
    fun getChiefComplaintDB(patientID: String) {
        viewModelScope.launch {
            try {
                _chiefComplaintDB.value =visitReasonsAndCategoriesRepo.getChiefComplaintDBByPatientId(patientID)
            } catch (e: Exception) {
                Timber.d("Error in Getting Chief Complaint DB $e")
            }
        }
    }

    private fun getHigherHealthCareDropdown(){
        try{
            _higherHealthCare  = doctorMasterDataMaleRepo.getHigherHealthCenter()

        } catch (e: java.lang.Exception){
            Timber.d("Error in Getting Higher Health Care $e")
        }
    }
    private fun getCounsellingTypes(){
        try{
            _counsellingProvided = doctorMasterDataMaleRepo.getAllCounsellingList()
        } catch (e: java.lang.Exception){
            Timber.d("Error in getFormMaster $e")
        }
    }
    private fun getFormMaster(){
        try{
            _formMedicineDosage  = doctorMasterDataMaleRepo.getAllItemMasterList()
        } catch (e: java.lang.Exception){
            Timber.d("Error in getFormMaster $e")
        }
    }

    private fun getProcedureDropdown(){
        try{
            _procedureDropdown  = maleMasterDataRepository.getAllProcedureDropdown()
        } catch (e: java.lang.Exception){
            Timber.d("Error in Get Procedure $e")
        }
    }

    fun saveInvestigationToCache(investigationCaseRecord: InvestigationCaseRecord) {
        viewModelScope.launch {
            try {
                caseRecordeRepo.saveInvestigationToCatche(investigationCaseRecord)
            } catch (e: Exception) {
                Timber.e("Error in saving Investigation: $e")
            }
        }
    }

    fun saveDiagnosisToCache(diagnosisCaseRecord: DiagnosisCaseRecord) {
        viewModelScope.launch {
            try {
                caseRecordeRepo.saveDiagnosisToCatche(diagnosisCaseRecord)
            } catch (e: Exception) {
                Timber.e("Error in saving diagnosis: $e")
            }
        }
    }

    fun savePrescriptionToCache(prescriptionCaseRecord: PrescriptionCaseRecord) {
        viewModelScope.launch {
            try {
                caseRecordeRepo.savePrescriptionToCatche(prescriptionCaseRecord)
            } catch (e: Exception) {
                Timber.e("Error in saving Prescription: $e")
            }
        }
    }

    fun savePatientVitalInfoToCache(patientVitalsModel: PatientVitalsModel){
        viewModelScope.launch {
            try {
                vitalsRepo.saveVitalsInfoToCache(patientVitalsModel)
            } catch (e: Exception) {
                Timber.e("Error in saving vitals information : $e")
            }
        }
    }

    fun saveVisitDbToCatche(visitDB: VisitDB){
        viewModelScope.launch {
            try {
                visitRepo.saveVisitDbToCache(visitDB)
            }catch (e:Exception){
                Timber.e("Error in saving visit Db : $e")
            }
        }
    }

    fun saveChiefComplaintDbToCatche(chiefComplaintDB: ChiefComplaintDB){
        viewModelScope.launch {
            try {
                visitRepo.saveChiefComplaintDbToCache(chiefComplaintDB)
            }catch (e:Exception){
                Timber.e("Error in saving chieft complaint Db : $e")
            }
        }
    }

    fun savePatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
        viewModelScope.launch {
            try {
                val existingPatientVisitInfoSync = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID = patientVisitInfoSync.patientID, benVisitNo = patientVisitInfoSync.benVisitNo)
                if(existingPatientVisitInfoSync != null){
                    existingPatientVisitInfoSync.nurseDataSynced = SyncState.UNSYNCED
                    existingPatientVisitInfoSync.doctorDataSynced = SyncState.UNSYNCED
                    existingPatientVisitInfoSync.createNewBenFlow = patientVisitInfoSync.createNewBenFlow
                    existingPatientVisitInfoSync.nurseFlag = 9
                    existingPatientVisitInfoSync.doctorFlag = patientVisitInfoSync.doctorFlag
                    patientVisitInfoSyncRepo.insertPatientVisitInfoSync(existingPatientVisitInfoSync)
                }
                else{
                    patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
                }
            }catch (e:Exception){
                Timber.e("Error in saving chieft complaint Db : $e")
            }
        }
    }

    suspend fun updateDoctorDataSubmitted(benVisitInfo: PatientDisplayWithVisitInfo, doctorFlag: Int){
        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = benVisitInfo.patient.patientID,
            nurseDataSynced = benVisitInfo.nurseDataSynced,
            doctorDataSynced = SyncState.UNSYNCED,
            createNewBenFlow = benVisitInfo.createNewBenFlow,
            benVisitNo = benVisitInfo.benVisitNo!!,
            benFlowID = benVisitInfo.benFlowID,
            nurseFlag = 9,
            doctorFlag = doctorFlag,
            pharmacist_flag = benVisitInfo.pharmacist_flag,
        )
        patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
    }

    suspend fun hasUnSyncedNurseData(patientId : String) : Boolean{
        return patientVisitInfoSyncRepo.hasUnSyncedNurseData(patientId);
    }

    suspend fun getLastVisitInfoSync(patientId : String) : PatientVisitInfoSync?{
        return patientVisitInfoSyncRepo.getLastVisitInfoSync(patientId);
    }

    suspend fun getSinglePatientDoctorDataNotSubmitted(patientId : String) : PatientVisitInfoSync?{
        return patientVisitInfoSyncRepo.getSinglePatientDoctorDataNotSubmitted(patientId);
    }

   suspend fun getTestNameTypeMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getProcedureTypeByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }
    suspend fun getReferNameTypeMap(): Map<Int, String> {
        return try {
            doctorMasterDataMaleRepo.getHigherHealthTypeByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }

}
