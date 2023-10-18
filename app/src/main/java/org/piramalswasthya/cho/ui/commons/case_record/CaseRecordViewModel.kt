package org.piramalswasthya.cho.ui.commons.case_record

import android.content.Context
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
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.InvestigationDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
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
import org.piramalswasthya.cho.model.ProcedureDataWithComponent
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.ProcedureRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import org.piramalswasthya.cho.work.WorkerUtils
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
    private val procedureRepo: ProcedureRepo,
    private val visitRepo: VisitReasonsAndCategoriesRepo,
    private val patientRepo: PatientRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val prescriptionDao: PrescriptionDao,
    private val caseRecordeDao: CaseRecordeDao,
    private val investigationDao: InvestigationDao,
): ViewModel() {

    private val _isDataDeleted = MutableLiveData<Boolean>(false)
    val isDataDeleted: MutableLiveData<Boolean>
        get() = _isDataDeleted


    private val _isDataSaved = MutableLiveData<Boolean>(false)
    val isDataSaved: MutableLiveData<Boolean>
        get() = _isDataSaved

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

    private var _labReportList= MutableLiveData<List<ProcedureDataWithComponent>>()
    val labReportList: LiveData<List<ProcedureDataWithComponent>>
        get() = _labReportList

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
                    vitalsRepo.getVitalsDetailsByPatientIDAndBenVisitNoForFollowUp(patientID)

            } catch (e: java.lang.Exception) {
                Timber.d("Error in Getting Higher Health Care $e")
            }
        }
    }
    fun getChiefComplaintDB(patientID: String,benVisitNo: Int) {
        viewModelScope.launch {
            try {
                _chiefComplaintDB.value =visitReasonsAndCategoriesRepo.getChiefComplaintDBByPatientId(patientID, benVisitNo)
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
   fun getLabList(patientID: String,benVisitNo: Int){
       viewModelScope.launch {
           try{
               _labReportList.value = procedureRepo.getProceduresWithComponent(patientID,benVisitNo)
           }catch (e:Exception){
               Timber.e("Error in getting Procedure: $e")
           }
       }
   }
    suspend fun saveInvestigationToCache(investigationCaseRecord: InvestigationCaseRecord) {
        caseRecordeRepo.saveInvestigationToCatche(investigationCaseRecord)
    }

    suspend fun saveDiagnosisToCache(diagnosisCaseRecord: DiagnosisCaseRecord) {
        caseRecordeRepo.saveDiagnosisToCatche(diagnosisCaseRecord)
    }

    suspend fun savePrescriptionToCache(prescriptionCaseRecord: PrescriptionCaseRecord) {
        caseRecordeRepo.savePrescriptionToCatche(prescriptionCaseRecord)
    }

    suspend fun savePatientVitalInfoToCache(patientVitalsModel: PatientVitalsModel){
        vitalsRepo.saveVitalsInfoToCache(patientVitalsModel)
    }

    suspend fun saveVisitDbToCatche(visitDB: VisitDB){
        visitRepo.saveVisitDbToCache(visitDB)
    }

    suspend fun saveChiefComplaintDbToCatche(chiefComplaintDB: ChiefComplaintDB){
        visitRepo.saveChiefComplaintDbToCache(chiefComplaintDB)
    }

    fun deleteOldDoctorData(patientID: String, benVisitNo: Int){
        viewModelScope.launch {
            try {
                _isDataDeleted.value = false
                prescriptionDao.deletePrescriptionByPatientIdAndBenVisitNo(patientID, benVisitNo)
                investigationDao.deleteInvestigationCaseRecordByPatientIdAndBenVisitNo(patientID, benVisitNo)
                caseRecordeDao.deleteDiagnosisByPatientIdAndBenVisitNo(patientID, benVisitNo)
                _isDataDeleted.value = true
            }catch (e:Exception){
                Timber.e("Error in saving chieft complaint Db : $e")
            }
        }
    }

    suspend fun savePatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
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
    }

    suspend fun updateDoctorDataSubmitted(benVisitInfo: PatientDisplayWithVisitInfo, doctorFlag: Int){
//        val patientVisitInfoSync = PatientVisitInfoSync(
//            patientID = benVisitInfo.patient.patientID,
//            nurseDataSynced = benVisitInfo.nurseDataSynced,
//            doctorDataSynced = SyncState.UNSYNCED,
//            createNewBenFlow = benVisitInfo.createNewBenFlow,
//            benVisitNo = benVisitInfo.benVisitNo!!,
//            benFlowID = benVisitInfo.benFlowID,
//            nurseFlag = 9,
//            doctorFlag = doctorFlag,
//            labtechFlag = benVisitInfo.labtechFlag,
//            pharmacist_flag = benVisitInfo.pharmacist_flag,
//        )
        var labtechFlag = benVisitInfo.labtechFlag!!
        if(benVisitInfo.doctorFlag == 3){
            labtechFlag = 1
        }
        patientVisitInfoSyncRepo.updateOnlyDoctorDataSubmitted(
            nurseFlag = 9,
            doctorFlag = doctorFlag,
            labtechFlag = labtechFlag,
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitInfo.benVisitNo!!
        )
    }

    fun saveDoctorData(diagnosisList: List<DiagnosisCaseRecord>, investigation: InvestigationCaseRecord,
                       prescriptionList: List<PrescriptionCaseRecord>, benVisitInfo: PatientDisplayWithVisitInfo, doctorFlag: Int){
        viewModelScope.launch {
            try {
                diagnosisList.forEach {
                    saveDiagnosisToCache(it)
                }
                saveInvestigationToCache(investigation)
                prescriptionList.forEach {
                    savePrescriptionToCache(it)
                }
                updateDoctorDataSubmitted(benVisitInfo, doctorFlag)
                _isDataSaved.value = true
            } catch (e: Exception){
                _isDataSaved.value = false
            }
        }
    }

    fun saveNurseAndDoctorData(visitDB: VisitDB, chiefComplaints: List<ChiefComplaintDB>, patientVitals: PatientVitalsModel,
                               diagnosisList: List<DiagnosisCaseRecord>, investigation: InvestigationCaseRecord,
                               prescriptionList: List<PrescriptionCaseRecord>, patientVisitInfoSync: PatientVisitInfoSync){
        viewModelScope.launch {
            try {
                saveVisitDbToCatche(visitDB)
                chiefComplaints.forEach {
                    saveChiefComplaintDbToCatche(it)
                }
                savePatientVitalInfoToCache(patientVitals)
                diagnosisList.forEach {
                    saveDiagnosisToCache(it)
                }
                saveInvestigationToCache(investigation)
                prescriptionList.forEach {
                    savePrescriptionToCache(it)
                }
                savePatientVisitInfoSync(patientVisitInfoSync)
                _isDataSaved.value = true
            } catch (e: Exception){
                _isDataSaved.value = false
            }
        }
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
