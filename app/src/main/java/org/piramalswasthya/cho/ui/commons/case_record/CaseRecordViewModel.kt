package org.piramalswasthya.cho.ui.commons.case_record


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.InvestigationDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.EarDiagnosisAssessment
import org.piramalswasthya.cho.model.ElderlyHealthAssessment
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.MentalHealthScreeningCache
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import org.piramalswasthya.cho.model.OphthalmicVisit
import org.piramalswasthya.cho.model.OralHealth
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.ProcedureDataWithComponent
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport
import org.piramalswasthya.cho.model.ThroatDiagnosisAssessment
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.EarDiagnosisRepo
import org.piramalswasthya.cho.repositories.ElderlyHealthRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.MentalHealthScreeningRepo
import org.piramalswasthya.cho.repositories.NoseDiagnosisRepo
import org.piramalswasthya.cho.repositories.OphthalmicRepository
import org.piramalswasthya.cho.repositories.OralHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.PainAndSymptomAssessmentRepo
import org.piramalswasthya.cho.repositories.PrescriptionTemplateRepo
import org.piramalswasthya.cho.repositories.ProcedureRepo
import org.piramalswasthya.cho.repositories.PsychosocialCaregiverSupportRepo
import org.piramalswasthya.cho.repositories.ThroatDiagnosisRepo
import org.piramalswasthya.cho.repositories.UserRepo
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
    private val earDiagnosisRepo: EarDiagnosisRepo,
    private val noseDiagnosisRepo: NoseDiagnosisRepo,
    private val throatDiagnosisRepo: ThroatDiagnosisRepo,
    private val oralHealthRepo: OralHealthRepo,
    private val ophthalmicRepository: OphthalmicRepository,
    private val elderlyHealthRepo: ElderlyHealthRepo,
    private val mentalHealthScreeningRepo: MentalHealthScreeningRepo,
    private val painAndSymptomAssessmentRepo: PainAndSymptomAssessmentRepo,
    private val psychosocialCaregiverSupportRepo: PsychosocialCaregiverSupportRepo,
    preferenceDao: PreferenceDao,
    private val procedureRepo: ProcedureRepo,
    private val visitRepo: VisitReasonsAndCategoriesRepo,
    private val patientRepo: PatientRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val prescriptionDao: PrescriptionDao,
    private val caseRecordeDao: CaseRecordeDao,
    private val investigationDao: InvestigationDao,
    private val userRepo: UserRepo,
    private val templateRepo: PrescriptionTemplateRepo,
    private val benFlowRepo: BenFlowRepo,
    private val caseClosureManager: org.piramalswasthya.cho.helpers.CaseClosureManager
): ViewModel() {
    val userId  = userRepo.getLoggedInUserAsFlow()
    private val _isDataDeleted = MutableLiveData<Boolean>(false)
    val isDataDeleted: MutableLiveData<Boolean>
        get() = _isDataDeleted

    private var _previousTests = MutableLiveData<InvestigationCaseRecord?>(null)
    val previousTests: LiveData<InvestigationCaseRecord?>
        get() = _previousTests

    private val _isDataSaved = MutableLiveData<Boolean>(false)
    val isDataSaved: MutableLiveData<Boolean>
        get() = _isDataSaved

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _requiresClosureConfirmation = MutableLiveData<Boolean>(false)
    val requiresClosureConfirmation: LiveData<Boolean>
        get() = _requiresClosureConfirmation

    private val _isClickedSS=MutableLiveData<Boolean>(false)

    val isClickedSS: MutableLiveData<Boolean>
        get() = _isClickedSS

    private val _diagnosisVal=MutableLiveData<Boolean>(false)
    val diagnosisVal: MutableLiveData<Boolean>
        get() = _diagnosisVal

    private var _formMedicineDosage: LiveData<List<ItemMasterList>>
    val formMedicineDosage: LiveData<List<ItemMasterList>>
        get() = _formMedicineDosage
    var userIDVAl:Int? = null
    val tempDB= userId.transformLatest {
        it?.let {
            emit(templateRepo.getProceduresWithComponent(it) )
        }
    }.asLiveData()





    val labReportProcedureTypes = mutableListOf<String>()
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

//    private val _visitReason = MutableLiveData<String?>()
//    val visitReason: LiveData<String?>
//        get() = _visitReason
//  private val _visitDate = MutableLiveData<String?>()
//    val visitDate: LiveData<String?>
//        get() = _visitDate



    private val _benFlows = MutableLiveData<List<BenFlow>?>()
    val benFlows: LiveData<List<BenFlow>?> = _benFlows


    init {
        viewModelScope.launch {
            userIDVAl = userId.first()

        }
        _counsellingProvided = MutableLiveData()
        getCounsellingTypes()
        _formMedicineDosage = MutableLiveData()
        getFormMaster()
        _procedureDropdown = MutableLiveData()
        getProcedureDropdown()
        _higherHealthCare = MutableLiveData()
        getHigherHealthCareDropdown()
//        getLoggedInUserDetails()
    }

    fun savePrescriptionTemp(prescriptionTemplateDB: PrescriptionTemplateDB){
        viewModelScope.launch {
            templateRepo.savePrescriptionTemplateToCache(prescriptionTemplateDB)
        }
    }
    suspend fun getPrescriptionForVisitNumAndPatientId(benVisitInfo: PatientDisplayWithVisitInfo):List<PrescriptionCaseRecord?>{
            return caseRecordeRepo.getPrescriptionByPatientIDAndVisitNumber(benVisitInfo)
    }
    
    suspend fun getDispensedPrescriptionsForVisitNumAndPatientId(patientID: String, benVisitNo: Int): List<PrescriptionCaseRecord?> {
        return try {
            val latestVisitInfo = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
                patientID,
                benVisitNo
            )
            // Get ALL prescriptions for this visit (there may be multiple if medicine was dispensed multiple times)
            val prescriptions = prescriptionDao.getPrescriptionsByPatientIdAndBenVisitNo(patientID, benVisitNo)
            if (prescriptions.isNullOrEmpty()) {
                Timber.d("No dispensed prescriptions found for patientID=$patientID, benVisitNo=$benVisitNo")
                emptyList()
            } else {
                val pharmacistFlag = latestVisitInfo?.pharmacist_flag ?: 0
                // When pharmacist is pending, latest prescription represents current editable cycle (not dispensed yet).
                // Older records in this visit are already dispensed and should stay visible as history.
                val dispensedPrescriptionRows =
                    (if (pharmacistFlag == 9) prescriptions else prescriptions.drop(1))
                        .sortedBy { it.id }

                if (dispensedPrescriptionRows.isEmpty()) {
                    Timber.d("No dispensed history rows for patientID=$patientID, benVisitNo=$benVisitNo, pharmacist_flag=$pharmacistFlag")
                    return emptyList()
                }

                Timber.d(
                    "Found ${dispensedPrescriptionRows.size} dispensed prescription records for patientID=$patientID, benVisitNo=$benVisitNo, pharmacist_flag=$pharmacistFlag"
                )
                // Get prescribed drugs from ALL prescriptions (to handle multiple dispensing cycles)
                val allDispensedDrugs = mutableListOf<PrescriptionCaseRecord>()
                dispensedPrescriptionRows.forEach { prescription ->
                    val prescribedDrugs = prescriptionDao.getPrescribedDrugs(prescription.id)
                    prescribedDrugs?.forEach { drug ->
                        allDispensedDrugs.add(
                            PrescriptionCaseRecord(
                                prescriptionCaseRecordId = "dispensed_${drug.id}",
                                itemId = drug.drugID.toInt(),
                                frequency = drug.frequency,
                                duration = drug.duration,
                                instructions = drug.instructions,
                                unit = drug.durationUnit,
                                patientID = patientID,
                                benFlowID = prescription.benFlowID,
                                benVisitNo = benVisitNo
                            )
                        )
                    }
                }
                Timber.d("Total dispensed drugs across all prescriptions: ${allDispensedDrugs.size}")
                allDispensedDrugs
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting dispensed prescriptions")
            emptyList()
        }
    }
    suspend fun getProvisionalDiagnosisForVisitNumAndPatientId(benVisitInfo: PatientDisplayWithVisitInfo):List<DiagnosisCaseRecord?>{
        return caseRecordeRepo.getDiagnosisByPatientIDAndVisitNumber(benVisitInfo)
    }
    fun savePrescriptionTempToServer(prescriptionTemplateDB: List<PrescriptionTemplateDB>){
        viewModelScope.launch {
            templateRepo.saveTemplateToServer(prescriptionTemplateDB)
        }
    }
    fun getPatientDisplayListForDoctorByPatient(patientID: String) : Flow<List<PatientDisplayWithVisitInfo>> {
        return patientVisitInfoSyncRepo.getPatientDisplayListForDoctorByPatient(patientID)
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

    suspend fun getChiefComplaintByPatientAndVisit(patientID: String, benVisitNo: Int): List<ChiefComplaintDB> {
        return try {
            visitReasonsAndCategoriesRepo.getChiefComplaintDBByPatientId(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getChiefComplaintByPatientAndVisit $e")
            emptyList()
        }
    }

    suspend fun getVitalsByPatientAndVisit(patientID: String, benVisitNo: Int): PatientVitalsModel? {
        return try {
            vitalsRepo.getPatientVitalsByPatientIDAndBenVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getVitalsByPatientAndVisit $e")
            null
        }
    }

    suspend fun getEarDiagnosisByPatientAndVisit(patientID: String, benVisitNo: Int): EarDiagnosisAssessment? {
        return try {
            earDiagnosisRepo.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getEarDiagnosisByPatientAndVisit $e")
            null
        }
    }

    suspend fun getLatestChiefComplaints(patientID: String): List<ChiefComplaintDB> {
        return try {
            visitReasonsAndCategoriesRepo.getChiefComplaintsByPatientAndBenForFollowUp(patientID)
        } catch (e: Exception) {
            Timber.d("Error in getLatestChiefComplaints $e")
            emptyList()
        }
    }

    suspend fun getLatestVitals(patientID: String): PatientVitalsModel? {
        return try {
            vitalsRepo.getVitalsDetailsByPatientIDAndBenVisitNoForFollowUp(patientID)
        } catch (e: Exception) {
            Timber.d("Error in getLatestVitals $e")
            null
        }
    }

    suspend fun getLatestEarDiagnosis(patientID: String): EarDiagnosisAssessment? {
        return try {
            earDiagnosisRepo.getAssessmentByPatientId(patientID)
        } catch (e: Exception) {
            Timber.d("Error in getLatestEarDiagnosis $e")
            null
        }
    }

    suspend fun getVisitCategoryByPatientAndVisit(patientID: String, benVisitNo: Int): String? {
        return try {
            visitReasonsAndCategoriesRepo
                .getVisitDbByPatientIDAndBenVisitNo(patientID, benVisitNo)
                ?.category
        } catch (e: Exception) {
            Timber.d("Error in getVisitCategoryByPatientAndVisit $e")
            null
        }
    }

    suspend fun getNoseDiagnosisByPatientAndVisit(patientID: String, benVisitNo: Int): NoseDiagnosisAssessment? {
        return try {
            noseDiagnosisRepo.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getNoseDiagnosisByPatientAndVisit $e")
            null
        }
    }

    suspend fun getThroatDiagnosisByPatientAndVisit(patientID: String, benVisitNo: Int): ThroatDiagnosisAssessment? {
        return try {
            throatDiagnosisRepo.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getThroatDiagnosisByPatientAndVisit $e")
            null
        }
    }

    suspend fun getOralHealthByPatientAndVisit(patientID: String, benVisitNo: Int): OralHealth? {
        return try {
            oralHealthRepo.getByPatientIdAndVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getOralHealthByPatientAndVisit $e")
            null
        }
    }

    suspend fun getOphthalmicByPatientAndVisit(patientID: String, benVisitNo: Int): OphthalmicVisit? {
        return try {
            ophthalmicRepository.getOphthalmicVisit(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getOphthalmicByPatientAndVisit $e")
            null
        }
    }

    suspend fun getElderlyByPatientAndVisit(patientID: String, benVisitNo: Int): ElderlyHealthAssessment? {
        return try {
            elderlyHealthRepo.getAssessment(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getElderlyByPatientAndVisit $e")
            null
        }
    }

    suspend fun getMentalByPatientAndVisit(patientID: String, benVisitNo: Int): MentalHealthScreeningCache? {
        return try {
            mentalHealthScreeningRepo.getScreeningByPatientIdAndVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getMentalByPatientAndVisit $e")
            null
        }
    }

    suspend fun getPainAssessmentByPatientAndVisit(patientID: String, benVisitNo: Int): PainAndSymptomAssessment? {
        return try {
            painAndSymptomAssessmentRepo.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getPainAssessmentByPatientAndVisit $e")
            null
        }
    }

    suspend fun getPsychosocialByPatientAndVisit(patientID: String, benVisitNo: Int): PsychosocialCaregiverSupport? {
        return try {
            psychosocialCaregiverSupportRepo.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
        } catch (e: Exception) {
            Timber.d("Error in getPsychosocialByPatientAndVisit $e")
            null
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
     fun getFormMaster(){
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
        // Reset immediately so observer in UI never consumes stale "true" from a previous run.
        _isDataDeleted.value = false
        viewModelScope.launch {
            try {
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
            existingPatientVisitInfoSync.pharmacist_flag = patientVisitInfoSync.pharmacist_flag
            existingPatientVisitInfoSync.visitDate = patientVisitInfoSync.visitDate
            existingPatientVisitInfoSync.visitCategory = "General OPD"
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(existingPatientVisitInfoSync)
        }
        else{
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
        }
    }

    suspend fun updateDoctorDataSubmitted(benVisitInfo: PatientDisplayWithVisitInfo, doctorFlag: Int){
        val latestVisitInfo = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
            benVisitInfo.patient.patientID,
            benVisitInfo.benVisitNo!!
        )
        val currentLabtechFlag = latestVisitInfo?.labtechFlag ?: benVisitInfo.labtechFlag ?: 0
        patientVisitInfoSyncRepo.updateOnlyDoctorDataSubmitted(
            nurseFlag = 9,
            doctorFlag = doctorFlag,
            labtechFlag = currentLabtechFlag,
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitInfo.benVisitNo!!
        )
    }

    fun closeCaseManually(benVisitInfo: PatientDisplayWithVisitInfo) {
        _isDataSaved.value = false
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // Validate closure conditions using CaseClosureManager
                val (canClose, errorMsg) = withContext(Dispatchers.IO) {
                    caseClosureManager.canManuallyClose(benVisitInfo)
                }
                
                if (!canClose) {
                    _errorMessage.value = errorMsg
                    _isDataSaved.value = false
                    Timber.w("Cannot close case: $errorMsg")
                    return@launch
                }
                
                withContext(Dispatchers.IO) {
                    patientVisitInfoSyncRepo.updateOnlyDoctorDataSubmitted(
                        nurseFlag = 9,
                        doctorFlag = 9,
                        labtechFlag = benVisitInfo.labtechFlag ?: 0,
                        patientID = benVisitInfo.patient.patientID,
                        benVisitNo = benVisitInfo.benVisitNo!!
                    )
                }
                _isDataSaved.value = true
                Timber.d("Case closed successfully for ${benVisitInfo.patient.patientID}/${benVisitInfo.benVisitNo}")
            } catch (e: Exception) {
                _isDataSaved.value = false
                _errorMessage.value = "Error closing case: ${e.message}"
                Timber.e(e, "Error closing case")
            }
        }
    }

    /**
     * Checks if manual closure confirmation dialog should be shown.
     * Call this before attempting to close the case.
     */
    fun checkClosureRequirements(benVisitInfo: PatientDisplayWithVisitInfo) {
        viewModelScope.launch {
            try {
                val requiresConfirmation = withContext(Dispatchers.IO) {
                    caseClosureManager.requiresManualClosureConfirmation(benVisitInfo)
                }
                _requiresClosureConfirmation.value = requiresConfirmation
            } catch (e: Exception) {
                Timber.e(e, "Error checking closure requirements")
                _requiresClosureConfirmation.value = false
            }
        }
    }

    fun saveDoctorData(diagnosisList: List<DiagnosisCaseRecord>, investigation: InvestigationCaseRecord,
                       prescriptionList: List<PrescriptionCaseRecord>, benVisitInfo: PatientDisplayWithVisitInfo, doctorFlag: Int){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    diagnosisList.forEach {
                        saveDiagnosisToCache(it)
                    }
                    saveInvestigationToCache(investigation)
                    prescriptionList.forEach {
                        savePrescriptionToCache(it)
                    }
                    updateDoctorDataSubmitted(benVisitInfo, doctorFlag)
                    // When doctor has prescribed medicines (with or without test), move card to pharmacist module.
                    // Called after updateDoctorDataSubmitted so pharmacist_flag and visitCategory are set last and
                    // card shows in pharmacist list even when both test and prescription are selected together.
                    if (prescriptionList.isNotEmpty()) {
                        val visitInfoBeforePendingUpdate =
                            patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
                                benVisitInfo.patient.patientID,
                                benVisitInfo.benVisitNo!!
                            )
                        val wasPendingPharmacist =
                            (visitInfoBeforePendingUpdate?.pharmacist_flag
                                ?: benVisitInfo.pharmacist_flag
                                ?: 0) == 1
                        patientVisitInfoSyncRepo.updatePharmacistFlagToPending(
                            benVisitInfo.patient.patientID,
                            benVisitInfo.benVisitNo!!
                        )
                        // Keep pharmacist module in sync with latest doctor edits for this visit.
                        benFlowRepo.copyPrescriptionFromCaseRecordToPharmacistTable(
                            benVisitInfo,
                            replaceLatestPending = wasPendingPharmacist
                        )
                    }

                    val latestVisitInfo =
                        patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
                            benVisitInfo.patient.patientID,
                            benVisitInfo.benVisitNo!!
                        )
                    val visitSnapshotForClosure = if (latestVisitInfo != null) {
                        benVisitInfo.copy(
                            nurseFlag = latestVisitInfo.nurseFlag,
                            doctorFlag = latestVisitInfo.doctorFlag,
                            labtechFlag = latestVisitInfo.labtechFlag,
                            pharmacist_flag = latestVisitInfo.pharmacist_flag
                        )
                    } else {
                        benVisitInfo
                    }

                    // Check if case should auto-close (nothing prescribed / only medicine already dispensed scenario)
                    val shouldAutoClose = caseClosureManager.shouldAutoClose(visitSnapshotForClosure)
                    if (shouldAutoClose) {
                        Timber.d("Auto-closing case - nothing prescribed: ${benVisitInfo.patient.patientID}/${benVisitInfo.benVisitNo}")
                        patientVisitInfoSyncRepo.updateOnlyDoctorDataSubmitted(
                            nurseFlag = 9,
                            doctorFlag = 9,
                            labtechFlag = visitSnapshotForClosure.labtechFlag ?: 0,
                            patientID = benVisitInfo.patient.patientID,
                            benVisitNo = benVisitInfo.benVisitNo!!
                        )
                    }
                }
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
                // When medicines are prescribed, ensure card moves to pharmacist module (for test+medicine or medicine-only)
                if (prescriptionList.isNotEmpty()) {
                    patientVisitInfoSyncRepo.updatePharmacistFlagToPending(
                        patientVisitInfoSync.patientID,
                        patientVisitInfoSync.benVisitNo
                    )
                    // Fresh nurse+doctor flow also needs local pharmacist prescription rows,
                    // otherwise pharmacist tab may not show the newly assigned medicines.
                    runCatching {
                        val patient = patientRepo.getPatient(patientVisitInfoSync.patientID)
                        benFlowRepo.copyPrescriptionFromCaseRecordToPharmacistTable(
                            benVisitInfo = PatientDisplayWithVisitInfo(patient, patientVisitInfoSync),
                            replaceLatestPending = false
                        )
                    }.onFailure { e ->
                        Timber.e(e, "Failed to copy prescription rows to pharmacist table for ${patientVisitInfoSync.patientID}/${patientVisitInfoSync.benVisitNo}")
                    }
                }
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

    suspend fun getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int): PatientVisitInfoSync? {
        return patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID, benVisitNo)
    }

   suspend fun getTestNameTypeMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getProcedureTypeByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }

    suspend fun getPreviousTest(benVisitInfo: PatientDisplayWithVisitInfo){
        withContext(Dispatchers.IO) {
            _previousTests.postValue(caseRecordeRepo.getInvestigationCasesRecordByPatientIDAndVisitNumber(benVisitInfo))
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

    suspend fun getTemplatesByTemplateName(selectedString: String): List<PrescriptionTemplateDB?> {
        return templateRepo.getTemplateUsingTempName(selectedString)
    }

    suspend fun getAvailableStockForRule(itemID: Int): Int {
        return doctorMasterDataMaleRepo.getItemMasterQtyInHandById(itemID)
    }
//    fun getVisitReasonByBenFlowID(beneficiaryID: Long) {
//        viewModelScope.launch {
//            try {
//
//                val benFlowList = benFlowRepo.getBenFlowByBenFlowID(beneficiaryID)
//                val latest = benFlowList?.maxByOrNull { it.visitDate ?: "" }
//                _visitReason.value = latest?.VisitReason
//                _visitDate.value = latest?.visitDate
//            } catch (e: Exception) {
//                _visitReason.value = null
//                _visitDate.value = null
//            }
//        }
//    }

    fun getVisitReasonByBenFlowID(beneficiaryID: Long) {
        viewModelScope.launch {
            try {
                val benFlowList = benFlowRepo.getBenFlowByBenFlowID(beneficiaryID)
                _benFlows.value = benFlowList   // <-- store full list
            } catch (e: Exception) {
                _benFlows.value = emptyList()
            }
        }
    }


}
