package org.piramalswasthya.cho.ui.commons.pharmacist

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
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import javax.inject.Inject

@HiltViewModel
class PrescriptionBatchFormViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    savedStateHandle: SavedStateHandle,
    private val userRepo: UserRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val benFlowRepo: BenFlowRepo,
    private val patientRepo: PatientRepo
) : ViewModel() {

//    private var _loggedInUser: UserCache? = null
//    val loggedInUser: UserCache?
//        get() = _loggedInUser
//    private var _boolCall = MutableLiveData(false)
//    val boolCall: LiveData<Boolean>
//        get() = _boolCall
//
    var prescriptionForPharmacist : PrescriptionDTO? = null

    private var _prescriptions = MutableLiveData<PrescriptionDTO>(null)
    val prescriptions: LiveData<PrescriptionDTO>
        get() = _prescriptions

    enum class NetworkState {
        IDLE,
        LOADING,
        SUCCESS,
        FAILURE
    }

    private val _prescriptionObserver = MutableLiveData(NetworkState.IDLE)
    val prescriptionObserver: LiveData<NetworkState>
        get() = _prescriptionObserver

    init {
        getNetworkPrescriptionList()
    }

    fun getNetworkPrescriptionList() {
        viewModelScope.launch {
            _prescriptionObserver.value = NetworkState.SUCCESS
        }
    }

    suspend fun downloadPrescription(benVisitInfo : PatientDisplayWithVisitInfo) {
        withContext(Dispatchers.IO) {
            benFlowRepo.pullPrescriptionListData(benVisitInfo)
        }
    }

    suspend fun getPrescription(benVisitInfo : PatientDisplayWithVisitInfo) {
        withContext(Dispatchers.IO) {
            _prescriptions.postValue(patientRepo.getPrescriptions(benVisitInfo)?.get(0) ?: null)
        }
    }
//
//    @SuppressLint("StaticFieldLeak")
//    val context: Context = application.applicationContext
//
//    val state = savedStateHandle
//
//    val isEntitySaved = MutableLiveData<Boolean>()
//
//    suspend fun downloadProcedure(patientId: String) {
//        withContext(Dispatchers.IO) {
//            benFlowRepo.pullLabProcedureData(patientId)
//        }
//    }
//    suspend fun getPrescribedProcedures(patientId: String) {
//        withContext(Dispatchers.IO) {
//            _procedures.postValue(patientRepo.getProcedures(patientId))
//            Timber.d("fetched procedures")
//        }
//    }
//
//    fun saveEntity() {
//        viewModelScope.launch {
//            //map entity
//            isEntitySaved.value = true
//        }
//    }
//
//    fun getLoggedInUserDetails() {
//        viewModelScope.launch {
//            try {
//                _loggedInUser = userRepo.getUserCacheDetails()
//                _boolCall.value = true
//            } catch (e: Exception) {
//                Timber.d("Error in calling getLoggedInUserDetails() $e")
//                _boolCall.value = false
//            }
//        }
//    }
//
//    fun resetBool() {
//        _boolCall.value = false
//    }
//
//    fun saveLabData(dtos: List<ProcedureDTO>?, patientId: String) {
//        try {
//            dtos?.forEach { procedureDTO ->
//
//                viewModelScope.launch {
//                    var procedure =
//                        patientRepo.getProcedure(procedureDTO.benRegId, procedureDTO.procedureID)
//                    procedureDTO.compListDetails.forEach { componentDetailDTO ->
//                        Timber.d("mjf" + componentDetailDTO.testComponentName + ":" + componentDetailDTO.testResultValue)
//                        var componentDetails =
//                            patientRepo.getComponent(procedure.id, componentDetailDTO.testComponentID)
//                        componentDetails.testResultValue = componentDetailDTO.testResultValue
//                        componentDetails.remarks = componentDetailDTO.remarks
//                        patientRepo.updateComponentDetails(componentDetails)
//                    }
//
//                    // update sync state for lab data
//                    val patient = patientRepo.getPatient(patientId = patientId)
//                    val benFlow = benFlowRepo.getBenFlowByBenRegId(patient.beneficiaryRegID!!)
//
//                    val patientVisitInfoSync = benFlow?.benVisitNo?.let {
//                        patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientId,
//                            it
//                        )
//                    }
//                    patientVisitInfoSync?.labDataSynced = SyncState.UNSYNCED
//
//                    patientVisitInfoSync?.let {
//                        patientVisitInfoSyncRepo.insertPatientVisitInfoSync(it)
//                        WorkerUtils.labPushWorker(context)
//                    }
//                }
//            }
//
//        } catch (e: Exception) {
//            Timber.d("error saving lab records due to $e")
//        }
//    }
}