package org.piramalswasthya.cho.ui.commons.pharmacist

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import org.piramalswasthya.cho.model.PrescriptionBatchDTO
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.network.BenHealthDetails
import org.piramalswasthya.cho.network.BenificiarySaveResponse
import org.piramalswasthya.cho.network.GenerateOTPForCareContext
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.ValidateOTPAndCreateCareContextResponse
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.BenVisitRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PharmacistFormViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    savedStateHandle: SavedStateHandle,
    private val benVisitRepo: BenVisitRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val benFlowRepo: BenFlowRepo,
    private val patientRepo: PatientRepo,
    private val batchDao: BatchDao,
    private val userRepo: UserRepo,
    private val caseClosureManager: org.piramalswasthya.cho.helpers.CaseClosureManager
) : ViewModel() {


    private var _isDataSaved = MutableLiveData(false)
    val isDataSaved: LiveData<Boolean>
        get() = _isDataSaved

    private var _prescriptions = MutableLiveData<PrescriptionDTO>(null)
    val prescriptions: LiveData<PrescriptionDTO>
        get() = _prescriptions

    private val _isBenHealthInfoFetched = MutableLiveData(false)
    val isBenHealthInfoFetched: MutableLiveData<Boolean>
        get() = _isBenHealthInfoFetched

    private val _isOtpGenerated = MutableLiveData(false)
    val isOtpGenerated: MutableLiveData<Boolean>
        get() = _isOtpGenerated

    private val _isOtpVerified = MutableLiveData(false)
    val isOtpVerified: MutableLiveData<Boolean>
        get() = _isOtpVerified

    var benHealthInfo: BenHealthDetails? = null
    var txnId: GenerateOTPForCareContext? = null
    var response2: ValidateOTPAndCreateCareContextResponse? = null

//    private val _txnId = MutableLiveData("")
//    val txnId: MutableLiveData<String>
//        get() = _txnId

//    var careContext: String? = null

//    private val _careContext = MutableLiveData("")
//    val careContext: MutableLiveData<String>
//        get() = _careContext

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

    fun getBenHealthId(visitCode: Long?, benId: Long?, benRegId: Long?) {
        viewModelScope.launch {
            when (val response = patientRepo.getWorkLocationMappedAbdmFacility(visitCode, benId, benRegId)) {
                is NetworkResult.Success -> {
                    benHealthInfo = response.data as BenHealthDetails
                    _isBenHealthInfoFetched.value = benHealthInfo != null
                }
                is NetworkResult.Error -> {
                    _isBenHealthInfoFetched.value = false
                }
                else -> {}
            }
        }
    }

    fun generateOTPForCareContext() {
        viewModelScope.launch {
            when (val response = patientRepo.generateOTPForCareContext(benHealthInfo?.healthId!!, benHealthInfo?.healthIdNumber!!)) {
                is NetworkResult.Success -> {
                    txnId = response.data as GenerateOTPForCareContext
                    _isOtpGenerated.value = txnId != null
                }
                is NetworkResult.Error -> {
                    _isOtpGenerated.value  = false
                }
                else -> {}
            }
        }
    }

    fun validateOTPAndCreateCareContext(otp: String, beneficiaryID: Long, visitCode: Long, visitCategory: String) {
        viewModelScope.launch {
            when (val response = patientRepo.validateOTPAndCreateCareContext(otp, txnId?.txnId!!, beneficiaryID, benHealthInfo?.healthId!!, benHealthInfo?.healthIdNumber!!, visitCode, visitCategory)) {
                is NetworkResult.Success -> {
                    response2 = response.data as ValidateOTPAndCreateCareContextResponse
                    _isOtpVerified.value = txnId != null
                }
                is NetworkResult.Error -> {
                    _isOtpVerified.value  = false
                }
                else -> {}
            }
        }
    }

    suspend fun downloadPrescription(benVisitInfo : PatientDisplayWithVisitInfo) {
        withContext(Dispatchers.IO) {
            // Preserve local doctor-updated prescription rows; fetch from API only when local is empty.
            val localPrescription = patientRepo.getPrescriptions(benVisitInfo)
            if (localPrescription.isNullOrEmpty()) {
                benFlowRepo.pullPrescriptionListData(benVisitInfo)
            }
        }
    }

    suspend fun getPrescription(benVisitInfo : PatientDisplayWithVisitInfo) {
        withContext(Dispatchers.IO) {
            var listPrescription = patientRepo.getPrescriptions(benVisitInfo)
            if (listPrescription.isNullOrEmpty()) {
                // For unsynced local doctor submissions, pharmacist data may still exist only in case-record tables.
                benFlowRepo.copyPrescriptionFromCaseRecordToPharmacistTable(benVisitInfo)
                listPrescription = patientRepo.getPrescriptions(benVisitInfo)
            }
            if (listPrescription.isNullOrEmpty()) {
                benFlowRepo.pullPrescriptionListData(benVisitInfo)
                listPrescription = patientRepo.getPrescriptions(benVisitInfo)
            }
            Log.d("MyPrescription", "Prescription $listPrescription")
            if (listPrescription != null && listPrescription.isNotEmpty()) {
                _prescriptions.postValue(listPrescription[0])
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
        _isDataSaved.value = false
        viewModelScope.launch {
            try {
                val visitNo = benVisitInfo.benVisitNo ?: run {
                    _isDataSaved.value = false
                    return@launch
                }

                if(dtos!=null && dtos.itemList!=null){
                    dtos.itemList.forEach { item->
                        if(item.batchList!=null && item.batchList.isNotEmpty()){
                            val firstBatch = item.batchList.first()
                            Log.d("WU", "Batch1 ${firstBatch} ")
                            val availableBatch =  batchDao.getBatchByStockEntityId(firstBatch.itemStockEntryID.toLong())
                            Log.d("WU", "Batch2 ${availableBatch} ")
                            if(availableBatch!=null) {
                                val updatedBatch = availableBatch.copy(
                                    quantityInHand = availableBatch.quantityInHand - (item.qtyPrescribed ?: 0)
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
                patientVisitInfoSyncRepo.markPharmacistDispensedLocally(
                    benVisitInfo.patient.patientID,
                    visitNo
                )
                val latestVisitSnapshot = getLatestVisitSnapshot(benVisitInfo, visitNo)

                dtos?.let { prescriptionDTO ->
                    if(visitNo > 0){
                        val prescription = try {
                            if (prescriptionDTO.prescriptionID > 0) {
                                patientRepo.getPrescription(
                                    benVisitInfo.patient.patientID,
                                    visitNo,
                                    prescriptionDTO.prescriptionID
                                )
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        } ?: patientRepo.getLatestPrescription(
                            benVisitInfo.patient.patientID,
                            visitNo
                        )
                        prescription?.let {
                            it.issueType = dtos.issueType
                            patientRepo.updatePrescription(it)
                        }
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

                // Check if case should auto-close after medicine dispensed
                val shouldAutoClose = caseClosureManager.shouldAutoClose(latestVisitSnapshot)
                if (shouldAutoClose) {
                    Timber.d("Auto-closing case after medicine dispensed: ${benVisitInfo.patient.patientID}/${benVisitInfo.benVisitNo}")
                    patientVisitInfoSyncRepo.updateOnlyDoctorDataSubmitted(
                        nurseFlag = 9,
                        doctorFlag = 9,
                        labtechFlag = latestVisitSnapshot.labtechFlag ?: 0,
                        patientID = benVisitInfo.patient.patientID,
                        benVisitNo = visitNo
                    )
                }

                _isDataSaved.value = true

            } catch (e: Exception) {
                _isDataSaved.value = false
                Timber.e(e, "Error saving pharmacist data")
            }
        }
    }

    fun savePharmacistDataforManual(dtos: PrescriptionDTO?, benVisitInfo: PatientDisplayWithVisitInfo) {
        _isDataSaved.value = false
        viewModelScope.launch {
            try {
                val visitNo = benVisitInfo.benVisitNo ?: run {
                    _isDataSaved.value = false
                    return@launch
                }

                dtos?.itemList?.forEach { item ->
                    val prescribedQty = item.qtyPrescribed ?: 0
                    var remainingQty = prescribedQty

                    val selectedBatches = item.batchList?.filter { it.isSelected && it.dispenseQuantity > 0 } ?: emptyList()

                    if (selectedBatches.isNotEmpty()) {
                        selectedBatches.forEach { batch ->
                            if (remainingQty <= 0) return@forEach

                            val availableBatch = batchDao.getBatchByStockEntityId(batch.itemStockEntryID.toLong())
                            availableBatch?.let { dbBatch ->
                                val dispenseQty = minOf(batch.dispenseQuantity, remainingQty, dbBatch.quantityInHand)
                                val updatedQty = dbBatch.quantityInHand - dispenseQty

                                if (updatedQty <= 0) {
                                    batchDao.deleteBatch(dbBatch)
                                } else {
                                    batchDao.updateBatch(dbBatch.copy(quantityInHand = updatedQty))
                                }

                                remainingQty -= dispenseQty
                            } ?: run {
                                Log.w("PharmacistVM", "Batch not found in database: ${batch.itemStockEntryID}")
                            }
                        }

                        if (remainingQty > 0) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Insufficient stock for ${item.genericDrugName}. Only ${prescribedQty - remainingQty} units dispensed.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Log.w("PharmacistVM", "No batches selected for ${item.genericDrugName}")
                    }

                }

                patientVisitInfoSyncRepo.markPharmacistDispensedLocally(
                    benVisitInfo.patient.patientID,
                    visitNo
                )
                val latestVisitSnapshot = getLatestVisitSnapshot(benVisitInfo, visitNo)

                dtos.let { prescriptionDTO ->
                    if (visitNo > 0) {
                        val prescription = try {
                            if ((prescriptionDTO?.prescriptionID ?: 0L) > 0L) {
                                patientRepo.getPrescription(
                                    benVisitInfo.patient.patientID,
                                    visitNo,
                                    prescriptionDTO!!.prescriptionID
                                )
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        } ?: patientRepo.getLatestPrescription(
                            benVisitInfo.patient.patientID,
                            visitNo
                        )
                        prescription?.let {
                            it.issueType = dtos?.issueType
                            patientRepo.updatePrescription(it)
                        }
                    }
                }

                WorkerUtils.pharmacistPushWorker(context)

                // Check if case should auto-close after medicine dispensed
                val shouldAutoClose = caseClosureManager.shouldAutoClose(latestVisitSnapshot)
                if (shouldAutoClose) {
                    Timber.d("Auto-closing case after medicine dispensed (manual): ${benVisitInfo.patient.patientID}/${benVisitInfo.benVisitNo}")
                    patientVisitInfoSyncRepo.updateOnlyDoctorDataSubmitted(
                        nurseFlag = 9,
                        doctorFlag = 9,
                        labtechFlag = latestVisitSnapshot.labtechFlag ?: 0,
                        patientID = benVisitInfo.patient.patientID,
                        benVisitNo = visitNo
                    )
                }

                _isDataSaved.value = true
            } catch (e: Exception) {
                _isDataSaved.value = false
                Timber.e(e, "Error saving pharmacist data")
            }
        }
    }

    private suspend fun getLatestVisitSnapshot(
        benVisitInfo: PatientDisplayWithVisitInfo,
        visitNo: Int
    ): PatientDisplayWithVisitInfo {
        val latestVisit = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
            benVisitInfo.patient.patientID,
            visitNo
        )
        return if (latestVisit != null) {
            benVisitInfo.copy(
                nurseFlag = latestVisit.nurseFlag,
                doctorFlag = latestVisit.doctorFlag,
                labtechFlag = latestVisit.labtechFlag,
                pharmacist_flag = latestVisit.pharmacist_flag
            )
        } else {
            benVisitInfo.copy(pharmacist_flag = 9)
        }
    }

    suspend fun refreshBatchData(): Boolean {
        return try {
            val facilityID = userRepo.getLoggedInUser()?.facilityID ?: return false
            val result = benFlowRepo.getStockDetailsOfSubStore(facilityID)
            result is NetworkResult.Success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getBatchesForMedicine(drugID: Long): List<PrescriptionBatchDTO> {
        return try {
            // First try to get batches from local database
            var batches = batchDao.getBatchesByItemID(drugID.toInt())

            // If no batches found locally, try to refresh from server
            if (batches.isEmpty()) {
                val refreshSuccess = refreshBatchData()
                if (refreshSuccess) {
                    batches = batchDao.getBatchesByItemID(drugID.toInt())
                }
            }

            // Convert to PrescriptionBatchDTO format
            batches.filter { batch ->
                // Only include non-expired batches with available quantity
                val expiryDays = calculateExpiryInDays(batch.expiryDate)
                expiryDays > 0 && batch.quantityInHand > 0
            }.map { batch ->
                PrescriptionBatchDTO(
                    expiresIn = calculateExpiryInDays(batch.expiryDate),
                    batchNo = batch.batchNo,
                    expiryDate = convertDateFormat(batch.expiryDate),
                    itemStockEntryID = batch.stockEntityId.toInt(),
                    qty = batch.quantityInHand,
                    isSelected = false,
                    dispenseQuantity = 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun calculateExpiryInDays(expiryDate: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val expiry = sdf.parse(expiryDate)
            val today = Date()
            val diffInMillis = expiry?.time?.minus(today.time) ?: 0
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun convertDateFormat(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            false
        }
    }

}