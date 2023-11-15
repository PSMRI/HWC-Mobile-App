package org.piramalswasthya.cho.ui.commons.pharmacist

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
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
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.BenVisitRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PharmacistFormViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    savedStateHandle: SavedStateHandle,
    private val benVisitRepo: BenVisitRepo,
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
    private var _isDataSaved = MutableLiveData(false)
    val isDataSaved: LiveData<Boolean>
        get() = _isDataSaved

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
            val listPrescription = patientRepo.getPrescriptions(benVisitInfo)
            if(listPrescription!=null && listPrescription.size>0){
                _prescriptions.postValue(listPrescription.get(0) ?: null)
            }
        }
    }

    suspend fun getAllocationItemForPharmacist(prescriptionDTO : PrescriptionDTO) {
        withContext(Dispatchers.IO) {
            benFlowRepo.getAllocationItemForPharmacist(prescriptionDTO)
        }
    }
//
    @SuppressLint("StaticFieldLeak")
    val context: Context = application.applicationContext
//
    val state = savedStateHandle
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
    fun savePharmacistData(dtos: PrescriptionDTO?, benVisitInfo: PatientDisplayWithVisitInfo) {
        try {

            viewModelScope.launch {

                dtos?.let { prescriptionDTO ->
                    if(benVisitInfo.benVisitNo!=null){
                        val prescription = patientRepo.getPrescription(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo, prescriptionDTO.prescriptionID)
                        prescription.issueType = dtos.issueType
                        patientRepo.updatePrescription(prescription)
                    }
                }

                // update sync state for pharmacist data

//                patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
//                WorkerUtils.pharmacistPushWorker(context)

                patientVisitInfoSyncRepo.updatePharmacistDataUnsynced(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)

                val resp = benVisitRepo.savePharmacistData(dtos, benVisitInfo)
                if(resp){
//                    val patientVisitInfoSync = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
//                        benVisitInfo.patient.patientID,
//                        benVisitInfo.benVisitNo!!
//                    )!!
//                    patientVisitInfoSync.pharmacistDataSynced = SyncState.SYNCED
//                    patientVisitInfoSync.pharmacist_flag = 9
//
//                    patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
                    patientVisitInfoSyncRepo.updatePharmacistDataSynced(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo)
                    Toast.makeText(context, "Item Dispensed", Toast.LENGTH_SHORT).show()
                }
                else{
                    patientVisitInfoSyncRepo.updatePharmacistDataSynced(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo)
                    Toast.makeText(context, "Error occured while saving request", Toast.LENGTH_SHORT).show()
                }

                _isDataSaved.value = resp

            }

        } catch (e: Exception) {
            Timber.d("error saving lab records due to $e")
        }
    }

//    fun savePharmacistData(dtos: PrescriptionDTO?, benVisitInfo: PatientDisplayWithVisitInfo) {
//        try {
//
//            viewModelScope.launch {
//
//                val patientVisitInfoSync = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
//                    benVisitInfo.patient.patientID,
//                    benVisitInfo.benVisitNo!!
//                )!!
//                patientVisitInfoSync.pharmacistDataSynced = SyncState.UNSYNCED
//                patientVisitInfoSync.pharmacist_flag = 9
//
//                dtos?.let { prescriptionDTO ->
//                    val prescription = patientRepo.getPrescription(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo, prescriptionDTO.prescriptionID)
//                    prescription.issueType = dtos.issueType
//                    patientRepo.updatePrescription(prescription)
//                }
//
//                // update sync state for pharmacist data
//
//                patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
//                WorkerUtils.pharmacistPushWorker(context)
//
//                _isDataSaved.value = true
//
//            }
//
//        } catch (e: Exception) {
//            Timber.d("error saving lab records due to $e")
//        }
//    }
}