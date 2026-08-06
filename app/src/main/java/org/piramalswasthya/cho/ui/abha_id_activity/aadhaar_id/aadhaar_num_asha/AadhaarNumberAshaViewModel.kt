package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.aadhaar_num_asha


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.*
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AadhaarNumberAshaViewModel @Inject constructor(
    private var abhaIdRepo: AbhaIdRepo,
    private var benRepo: PatientRepo
) : ViewModel() {

    private val _state = MutableLiveData(AadhaarIdViewModel.State.IDLE)
    val state: LiveData<AadhaarIdViewModel.State>
        get() = _state

    private var _txnId = MutableLiveData<String?>(null)
    val txnId: LiveData<String?>
        get() = _txnId

    private var _ben = MutableLiveData<String?>(null)
    val ben: LiveData<String?>
        get() = _ben

    var responseData: CreateAbhaIdResponse? = null

    private var _mobileNumber = MutableLiveData<String?>(null)
    val mobileNumber: LiveData<String?>
        get() = _mobileNumber

    val aadhaarNumber = MutableLiveData<String>("")

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _otpMobileNumberMessage = MutableLiveData<String?>(null)
    val otpMobileNumberMessage: LiveData<String?>
        get() = _otpMobileNumberMessage

    fun resetState() {
        _state.value = AadhaarIdViewModel.State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun generateOtpClicked(aadhaarNo: String) {
        _state.value = AadhaarIdViewModel.State.LOADING
        generateAadhaarOtp(aadhaarNo)
    }

    private fun generateAadhaarOtp(aadhaarNo: String) {
        viewModelScope.launch {
            when (val result =
//                abhaIdRepo.generateOtpForAadhaarV2(AbhaGenerateAadhaarOtpRequest(aadhaarNo))) {
                abhaIdRepo.generateAadhaarOtpV3(AbhaGenerateAadhaarOtpRequest(
                    "",
                    listOf<String>("abha-enrol"),
                    "aadhaar",
                    aadhaarNo,
                    "aadhaar"
                ))) {
                is NetworkResult.Success -> {
                    _txnId.value = result.data.txnId
//                    _mobileNumber.value = result.data.mobileNumber
                    _otpMobileNumberMessage.value = result.data.message
                    _state.value = AadhaarIdViewModel.State.SUCCESS
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = AadhaarIdViewModel.State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = AadhaarIdViewModel.State.ERROR_NETWORK
                }
            }
        }
    }

    fun verifyBio(aadhaarNo: String, pid: String?) {
        _state.value = AadhaarIdViewModel.State.LOADING
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.verifyBio(AadhaarVerifyBioRequest(aadhaarNo, "FMR", pid.toString()))) {
                is NetworkResult.Success -> {
                    responseData = result.data
                    _txnId.value = result.data.txnId
                    _mobileNumber.value = result.data.mobile
                    _state.value = AadhaarIdViewModel.State.SUCCESS
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = AadhaarIdViewModel.State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = AadhaarIdViewModel.State.ERROR_NETWORK
                }
            }
        }
    }

    fun getBen(benId: Long) {
        viewModelScope.launch {
            benRepo.getBenFromId(benId)?.let {
                it.firstName?.let { first ->
                    _ben.value = first
                }
                it.lastName?.let { last ->
                    _ben.value += " $last"
                }
            }
        }
    }

}