package org.piramalswasthya.cho.ui.commons.pharmacist

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import org.piramalswasthya.cho.database.room.dao.BatchDao
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.BenVisitRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
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
    private val patientRepo: PatientRepo,
    private val batchDao: BatchDao
) : ViewModel() {


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
            Log.d("MyPrescription", "Prescription ${listPrescription}")
            if(listPrescription!=null && listPrescription.size>0){
                Log.d("MyPrescription", "inside")
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
    fun savePharmacistData(dtos: PrescriptionDTO?, benVisitInfo: PatientDisplayWithVisitInfo) {
        try {

            viewModelScope.launch {

               if(dtos!=null && dtos.itemList!=null){
                   dtos.itemList.forEach { item->
                       if(item.batchList!=null && item.batchList.isNotEmpty()){
                           val firstBatch = item.batchList.first()
                           Log.d("WU", "Batch1 ${firstBatch} ")
                           val availableBatch =  batchDao.getBatchByStockEntityId(firstBatch.itemStockEntryID.toLong())
                           Log.d("WU", "Batch2 ${availableBatch} ")
                           if(availableBatch!=null) {
                               val updatedBatch = availableBatch.copy(
                                   quantityInHand = availableBatch.quantityInHand - item.qtyPrescribed!!
                               )
                               Log.d("WU", "Batch3 ${updatedBatch}")
                               if(updatedBatch.quantityInHand<=0){
                                   batchDao.deleteBatch(availableBatch)
                               }else{
                                   batchDao.updateBatch(updatedBatch)
                               }
                           }
                       }else{
                           Toast.makeText(context, "Medicine not available", Toast.LENGTH_SHORT).show()
                       }
                   }
               }
                patientVisitInfoSyncRepo.updatePharmacistFlag(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
                patientVisitInfoSyncRepo.updatePharmacistDataSyncState(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo, SyncState.UNSYNCED)

                dtos?.let { prescriptionDTO ->
                    if(benVisitInfo.benVisitNo!=null){
                        val prescription = patientRepo.getPrescription(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo, prescriptionDTO.prescriptionID)
                        prescription.issueType = dtos.issueType
                        patientRepo.updatePrescription(prescription)
                    }
                }
                WorkerUtils.pharmacistPushWorker(context)

//                patientVisitInfoSyncRepo.updatePharmacistDataSyncing(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
//
//                val resp = benVisitRepo.savePharmacistData(dtos, benVisitInfo)
//                if(resp){
//                    patientVisitInfoSyncRepo.updatePharmacistDataSynced(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo)
//                    Toast.makeText(context, "Item Dispensed", Toast.LENGTH_SHORT).show()
//                }
//                else{
//                    patientVisitInfoSyncRepo.updatePharmacistDataSynced(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo)
//                    Toast.makeText(context, "Error occured while saving request", Toast.LENGTH_SHORT).show()
//                }

                _isDataSaved.value = true

            }

        } catch (e: Exception) {
            Timber.d("error saving lab records due to $e")
        }
    }

    fun savePharmacistDataforManual(dtos: PrescriptionDTO?, benVisitInfo: PatientDisplayWithVisitInfo) {
        try {
            viewModelScope.launch {

                dtos?.itemList?.forEach { item ->
                    val prescribedQty = item.qtyPrescribed ?: 0
                    var remainingQty = prescribedQty

                    val selectedBatches = item.batchList?.filter { it.isSelected && it.dispenseQuantity > 0 } ?: emptyList()

                    if (selectedBatches.isNotEmpty()) {
                        selectedBatches.forEach { batch ->
                            if (remainingQty <= 0) return@forEach

                            val availableBatch = batchDao.getBatchByStockEntityId(batch.itemStockEntryID.toLong())
                            availableBatch?.let {
                                val dispenseQty = minOf(batch.dispenseQuantity, remainingQty)
                                val updatedQty = it.quantityInHand - dispenseQty

                                if (updatedQty <= 0) {
                                    batchDao.deleteBatch(it)
                                } else {
                                    batchDao.updateBatch(it.copy(quantityInHand = updatedQty))
                                }

                                remainingQty -= dispenseQty
                            }
                        }

                        if (remainingQty > 0) {
                            Toast.makeText(context, "Insufficient stock for ${item.genericDrugName}", Toast.LENGTH_SHORT).show()
                        }

                    }

                }

                patientVisitInfoSyncRepo.updatePharmacistFlag(
                    benVisitInfo.patient.patientID,
                    benVisitInfo.benVisitNo!!
                )

                patientVisitInfoSyncRepo.updatePharmacistDataSyncState(
                    benVisitInfo.patient.patientID,
                    benVisitInfo.benVisitNo,
                    SyncState.UNSYNCED
                )

                dtos.let { prescriptionDTO ->
                    if (benVisitInfo.benVisitNo != null) {
                        val prescription = patientRepo.getPrescription(
                            benVisitInfo.patient.patientID,
                            benVisitInfo.benVisitNo,
                            prescriptionDTO!!.prescriptionID
                        )
                        prescription.issueType = dtos?.issueType
                        patientRepo.updatePrescription(prescription)
                    }
                }

                WorkerUtils.pharmacistPushWorker(context)

                _isDataSaved.value = true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving pharmacist data")
        }
    }

}