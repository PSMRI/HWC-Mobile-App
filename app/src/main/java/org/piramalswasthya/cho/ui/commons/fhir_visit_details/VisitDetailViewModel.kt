package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.PncRepo
import org.piramalswasthya.cho.repositories.ProcedureRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import timber.log.Timber
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class VisitDetailViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository,
    private val userRepo: UserRepo,
    private val vitalsRepo: VitalsRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val procedureRepo: ProcedureRepo,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val pncRepo: PncRepo,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val ecrRepo: EcrRepo,
    @ApplicationContext private val application: Context
) : ViewModel() {
    private var _subCatVisitList: LiveData<List<SubVisitCategory>>
    val subCatVisitList: LiveData<List<SubVisitCategory>>
        get() = _subCatVisitList

    private val _chiefComplaintDB = MutableLiveData<List<ChiefComplaintDB>>()
    val chiefComplaintDB: LiveData<List<ChiefComplaintDB>>
        get() = _chiefComplaintDB

    private var _vitalsDB: PatientVitalsModel? = null
    val vitalsDB: PatientVitalsModel?
        get() = _vitalsDB

    private val _isDataSaved = MutableLiveData<Boolean>(false)
    val isDataSaved: MutableLiveData<Boolean>
        get() = _isDataSaved

    private val _isLMPDateSaved = MutableLiveData<Boolean>(false)
    val isLMPDateSaved: MutableLiveData<Boolean>
        get() = _isLMPDateSaved

    private val _isDeliveryDateSaved = MutableLiveData<Boolean>(false)
    val isDeliveryDateSaved: MutableLiveData<Boolean>
        get() = _isDeliveryDateSaved


    private val _idPatientId = MutableLiveData<String?>(null)

    fun setPatientId(id: String) {
        _idPatientId.value = id
    }

    val lastVisitDate : LiveData<String?> = _idPatientId.switchMap {
        it?.let {
            visitReasonsAndCategoriesRepo.getVisitDbByPatientIDAndBenVisitNo(it)
        }
    }

    var base64String = ""
    var fileName = ""
    private var _chiefComplaintMaster: Flow<List<ChiefComplaintMaster>>
    val chiefComplaintMaster: Flow<List<ChiefComplaintMaster>>
        get() = _chiefComplaintMaster

    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser
    private var isFollowUpChecked: Boolean = false

    private var _boolCall = MutableLiveData(false)
    val boolCall: LiveData<Boolean>
        get() = _boolCall

    var lastAncVisitNumber = MutableLiveData<Int?>(null)

    var allActiveAncRecords = MutableLiveData<List<PregnantWomanAncCache>?>()

    var activePwrRecord = MutableLiveData<PregnantWomanRegistrationCache?>(null)

    var lastPncVisitNumber = MutableLiveData<Int?>(null)

    var allActivePncRecords = MutableLiveData<List<PNCVisitCache>?>()

    var activeDeliveryRecord = MutableLiveData<DeliveryOutcomeCache?>(null)

    var allEctRecords = MutableLiveData<List<EligibleCoupleTrackingCache>?>()

    var lastAnc = MutableLiveData<PregnantWomanAncCache?>()

    var lastEct = MutableLiveData<EligibleCoupleTrackingCache?>()

    init {
        _subCatVisitList = MutableLiveData()
        _chiefComplaintMaster = MutableStateFlow(emptyList())
        getSubCatVisitList()
        getChiefMasterComplaintList()
    }

    fun init(patientID: String){
        viewModelScope.launch {
            lastAncVisitNumber.value = getLastAncVisitNumber(patientID)
            allActiveAncRecords.value = getAllActiveAncRecords(patientID)
            activePwrRecord.value = getSavedActiveRecordObserve(patientID)

            lastPncVisitNumber.value = getLastPncVisitNumber(patientID)
            allActivePncRecords.value = getAllPNCsByPatId(patientID)
            activeDeliveryRecord.value = getDeliveryOutcome(patientID)

            allEctRecords.value = getAllECT(patientID)
            lastAnc.value = getLastAnc(patientID)
            lastEct.value = getLastEct(patientID)
        }
    }

    fun saveNurseDataToDb(visitDB: VisitDB, chiefComplaints: List<ChiefComplaintDB>,
                          patientVitals: PatientVitalsModel, patientVisitInfoSync: PatientVisitInfoSync){
        viewModelScope.launch {
            try {
                saveVisitDbToCatche(visitDB)
                chiefComplaints.forEach {
                    saveChiefComplaintDbToCatche(it)
                }
                savePatientVitalInfoToCache(patientVitals)
                savePatientVisitInfoSync(patientVisitInfoSync)
                _isDataSaved.value = true
            } catch (e: Exception){
                _isDataSaved.value = false
            }
        }
    }

    suspend fun savePatientVitalInfoToCache(patientVitalsModel: PatientVitalsModel){
        vitalsRepo.saveVitalsInfoToCache(patientVitalsModel)
    }

    suspend fun getLastAncVisitNumber(benId: String): Int? {
        return maternalHealthRepo.getLastVisitNumber(benId)
    }

    suspend fun getLastPncVisitNumber(benId: String): Int? {
        return pncRepo.getLastVisitNumber(benId)
    }

    suspend fun getSavedActiveRecordObserve(benId: String): PregnantWomanRegistrationCache? {
        return maternalHealthRepo.getSavedRegistrationRecord(benId)
    }

    fun savePregnantWomanRegistration(benId: String, lmpDate: Date) {
        viewModelScope.launch {
            val user = userRepo.getLoggedInUser()!!
            val pwr = PregnantWomanRegistrationCache(
                patientID = benId,
                lmpDate = lmpDate.time,
                createdBy = user.userName,
                syncState = SyncState.UNSYNCED,
                updatedBy = user.userName
            )
            maternalHealthRepo.persistRegisterRecord(pwr)
            _isLMPDateSaved.value = true
        }
    }

    suspend fun getDeliveryOutcome(benId: String): DeliveryOutcomeCache? {
        return deliveryOutcomeRepo.getDeliveryOutcome(benId)
    }

    suspend fun getAllECT(benId: String): List<EligibleCoupleTrackingCache> {
        return ecrRepo.getAllECT(benId)
    }

    suspend fun getLastAnc(benId: String): PregnantWomanAncCache? {
        return maternalHealthRepo.getLastAnc(benId)
    }

    suspend fun getLastEct(benId: String): EligibleCoupleTrackingCache? {
        return ecrRepo.getLatestEctByBenId(benId)
    }

    fun saveDeliveryOutcome(benId: String, deliveryDate: Date) {
        viewModelScope.launch {
            val user = userRepo.getLoggedInUser()!!
            val deliveryOutcomeCache = DeliveryOutcomeCache(
                patientID = benId,
                dateOfDelivery = deliveryDate.time,
                createdBy = user.userName,
                syncState = SyncState.UNSYNCED,
                updatedBy = user.userName,
                isActive = true,
            )
            deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcomeCache)
            _isDeliveryDateSaved.value = true
        }
    }

    suspend fun getAllActiveAncRecords(benId: String): List<PregnantWomanAncCache> {
        return maternalHealthRepo.getAllActiveAncRecords(benId)
    }

    suspend fun getAllPNCsByPatId(benId: String): List<PNCVisitCache> {
        return pncRepo.getAllPNCsByPatId(benId)
    }

    suspend fun savePatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
        val existingPatientVisitInfoSync = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID = patientVisitInfoSync.patientID, benVisitNo = patientVisitInfoSync.benVisitNo)
        if(existingPatientVisitInfoSync != null){
            existingPatientVisitInfoSync.nurseDataSynced = patientVisitInfoSync.nurseDataSynced
            existingPatientVisitInfoSync.doctorDataSynced = patientVisitInfoSync.doctorDataSynced
            existingPatientVisitInfoSync.createNewBenFlow = patientVisitInfoSync.createNewBenFlow
            existingPatientVisitInfoSync.nurseFlag = patientVisitInfoSync.nurseFlag
            existingPatientVisitInfoSync.doctorFlag = patientVisitInfoSync.doctorFlag
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(existingPatientVisitInfoSync)
        }
        else{
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
        }
    }
    suspend fun saveChiefComplaintDbToCatche(chiefComplaintDB: ChiefComplaintDB){
        visitReasonsAndCategoriesRepo.saveChiefComplaintDbToCache(chiefComplaintDB)
    }
    suspend fun saveVisitDbToCatche(visitDB: VisitDB){
        visitReasonsAndCategoriesRepo.saveVisitDbToCache(visitDB)
    }

    fun setIsFollowUp(boolean: Boolean) {
        isFollowUpChecked = boolean
    }

    fun getIsFollowUp(): Boolean {
        return isFollowUpChecked
    }

    suspend fun getVitalsDB(patientID: String) {
        _vitalsDB = vitalsRepo.getVitalsDetailsByPatientIDAndBenVisitNoForFollowUp(patientID)
    }

    fun getChiefComplaintDB(patientID: String) {
        viewModelScope.launch {
            try {
                _chiefComplaintDB.value =
                    visitReasonsAndCategoriesRepo.getChiefComplaintsByPatientAndBenForFollowUp(
                        patientID
                    )
            } catch (e: Exception) {
                Timber.d("Error in Getting Chief Complaint DB $e")
            }
        }
    }

    private fun getSubCatVisitList() {
        try {
            _subCatVisitList = maleMasterDataRepository.getAllSubCatVisit()
        } catch (e: Exception) {
            Timber.d("Error in getSubCatVisitList() $e")
        }
    }

    fun getTheProcedure(patientID: String, benVisitNo: Int) {
        viewModelScope.launch {
            val procedureList = procedureRepo.getProceduresWithComponent(patientID, benVisitNo)
            val list = procedureList
        }
    }

    private fun getChiefMasterComplaintList() {
        try {
            _chiefComplaintMaster = maleMasterDataRepository.getChiefMasterComplaint()
        } catch (e: Exception) {
            Timber.d("error in getChiefMasterComplaintList() $e")
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

    suspend fun getLastVisitInfoSync(patientId: String): PatientVisitInfoSync? {
        return patientVisitInfoSyncRepo.getLastVisitInfoSync(patientId);
    }

    suspend fun getChiefMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getChiefByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }

    fun resetBool() {
        _boolCall.value = false
    }

    fun setBase64Str(fileStr: String, file: String) {
        base64String = fileStr
        fileName = file
    }

}