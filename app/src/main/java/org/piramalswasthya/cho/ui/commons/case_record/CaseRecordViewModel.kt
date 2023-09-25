package org.piramalswasthya.cho.ui.commons.case_record

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.PastIllnessHistory
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

    init {
        _counsellingProvided = MutableLiveData()
        getCounsellingTypes()
        _formMedicineDosage = MutableLiveData()
        getFormMaster()
        _procedureDropdown = MutableLiveData()
        getProcedureDropdown()

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
            withContext(Dispatchers.IO) {
                val patient = patientRepo.getPatient(investigationCaseRecord.patientID)
                investigationCaseRecord.beneficiaryID = patient.beneficiaryID
                investigationCaseRecord.beneficiaryRegID = patient.beneficiaryRegID
                caseRecordeRepo.saveInvestigationToCatche(investigationCaseRecord)
            }
        } catch (e: Exception) {
            Timber.e("Error in saving Investigation: $e")
        }
    }
}
    fun saveDiagnosisToCache(diagnosisCaseRecord: DiagnosisCaseRecord) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val patient = patientRepo.getPatient(diagnosisCaseRecord.patientID)
                    diagnosisCaseRecord.beneficiaryID = patient.beneficiaryID
                    diagnosisCaseRecord.beneficiaryRegID = patient.beneficiaryRegID
                    caseRecordeRepo.saveDiagnosisToCatche(diagnosisCaseRecord)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving diagnosis: $e")
            }
        }
    }
    fun savePrescriptionToCache(prescriptionCaseRecord: PrescriptionCaseRecord) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val patient = patientRepo.getPatient(prescriptionCaseRecord.patientID)
                    prescriptionCaseRecord.beneficiaryID = patient.beneficiaryID
                    prescriptionCaseRecord.beneficiaryRegID = patient.beneficiaryRegID
                    caseRecordeRepo.savePrescriptionToCatche(prescriptionCaseRecord)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving Prescription: $e")
            }
        }
    }

    fun savePatientVitalInfoToCache(patientVitalsModel: PatientVitalsModel){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val patient = patientRepo.getPatient(patientVitalsModel.patientID)
                    patientVitalsModel.beneficiaryID = patient.beneficiaryID
                    patientVitalsModel.beneficiaryRegID = patient.beneficiaryRegID
                    vitalsRepo.saveVitalsInfoToCache(patientVitalsModel)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving vitals information : $e")
            }
        }
    }

    fun saveVisitDbToCatche(visitDB: VisitDB){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    val patient = patientRepo.getPatient(visitDB.patientID)
                    visitDB.beneficiaryID = patient.beneficiaryID
                    visitDB.beneficiaryRegID = patient.beneficiaryRegID
                    visitRepo.saveVisitDbToCache(visitDB)
                }
            }catch (e:Exception){
                Timber.e("Error in saving visit Db : $e")
            }
        }
    }

    fun saveChiefComplaintDbToCatche(chiefComplaintDB: ChiefComplaintDB){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    val patient = patientRepo.getPatient(chiefComplaintDB.patientID)
                    chiefComplaintDB.beneficiaryID = patient.beneficiaryID
                    chiefComplaintDB.beneficiaryRegID = patient.beneficiaryRegID
                    visitRepo.saveChiefComplaintDbToCache(chiefComplaintDB)
                }
            }catch (e:Exception){
                Timber.e("Error in saving chieft complaint Db : $e")
            }
        }
    }

    fun savePatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    val patient = patientRepo.getPatient(patientVisitInfoSync.patientID)
                    patientVisitInfoSync.beneficiaryID = patient.beneficiaryID
                    patientVisitInfoSync.beneficiaryRegID = patient.beneficiaryRegID
                    patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
                    patientVisitInfoSyncRepo.updateDoctorDataSubmitted(patientVisitInfoSync.patientID)
                }
            }catch (e:Exception){
                Timber.e("Error in saving chieft complaint Db : $e")
            }
        }
    }

   suspend fun getTestNameTypeMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getProcedureTypeByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }

}
