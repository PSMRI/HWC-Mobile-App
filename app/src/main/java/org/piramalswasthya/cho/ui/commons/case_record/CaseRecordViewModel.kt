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
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.CounsellingTypes
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.HistoryRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
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
    private val visitRepo: VisitReasonsAndCategoriesRepo

    ): ViewModel() {

    private var _formMedicineDosage: LiveData<List<ItemMasterList>>
    val formMedicineDosage: LiveData<List<ItemMasterList>>
        get() = _formMedicineDosage

    private var _counsellingTypes: LiveData<List<CounsellingTypes>>
    val counsellingTypes: LiveData<List<CounsellingTypes>>
        get() = _counsellingTypes

    private var _procedureDropdown: LiveData<List<ProceduresMasterData>>
    val procedureDropdown: LiveData<List<ProceduresMasterData>>
        get() = _procedureDropdown

    init {
        _counsellingTypes = MutableLiveData()
        getCounsellingTypes()
        _formMedicineDosage = MutableLiveData()
        getFormMaster()
        _procedureDropdown = MutableLiveData()
        getProcedureDropdown()

    }
    private fun getCounsellingTypes(){
        try{
            _counsellingTypes = doctorMasterDataMaleRepo.getAllCounsellingList()

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
                    visitRepo.saveChiefComplaintDbToCache(chiefComplaintDB)
                }
            }catch (e:Exception){
                Timber.e("Error in saving chieft complaint Db : $e")
            }
        }
    }
    
}
