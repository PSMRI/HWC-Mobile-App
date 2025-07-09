package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.find_abha

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.Abha
import org.piramalswasthya.cho.network.LoginGenerateOtpRequest
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.SearchAbhaRequest
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FindAbhaViewModel @Inject constructor(
    private var abhaIdRepo: AbhaIdRepo,
    private var benRepo: PatientRepo
) : ViewModel() {

    private val _state = MutableLiveData(AadhaarIdViewModel.State.IDLE)
    val state: LiveData<AadhaarIdViewModel.State>
        get() = _state

    private val _fnlState = MutableLiveData(AadhaarIdViewModel.State.IDLE)
    val fnlState: LiveData<AadhaarIdViewModel.State>
        get() = _fnlState

    private var _txnId = MutableLiveData<String?>(null)
    val txnId: LiveData<String?>
        get() = _txnId

    private var _fnlTxnId = MutableLiveData<String?>(null)
    val fnlTxnId: LiveData<String?>
        get() = _fnlTxnId

    private var _abha = MutableLiveData<List<Abha>?>(null)
    val abha: LiveData<List<Abha>?>
        get() = _abha

    private var _ben = MutableLiveData<String?>(null)
    val ben: LiveData<String?>
        get() = _ben

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

    fun searchAbhaClicked(mobileNo: String) {
        _state.value = AadhaarIdViewModel.State.LOADING
        searchAbha(mobileNo)
    }

    private fun searchAbha(mobileNo: String) {
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.searchAbha(
                    SearchAbhaRequest(
                    listOf<String>("search-abha"),
                    mobileNo
                )
                )) {
                is NetworkResult.Success -> {
                    _txnId.value = result.data.txnId
                    _abha.value = result.data.ABHA
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

    fun generateOtpClicked(index: String) {
        _state.value = AadhaarIdViewModel.State.LOADING
        generateAbhaOtp(index)
    }

    private fun generateAbhaOtp(index: String) {
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.generateAbhaOtp(
                    LoginGenerateOtpRequest(
                    listOf<String>("abha-login", "search-abha", "mobile-verify"),
                    "index",
                    index,
                    "abdm",
                    txnId.value!!
                )
                )) {
                is NetworkResult.Success -> {
                    _fnlTxnId.value = result.data.txnId
                    _fnlState.value = AadhaarIdViewModel.State.SUCCESS
                    _otpMobileNumberMessage.value = result.data.message
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _fnlState.value = AadhaarIdViewModel.State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _fnlState.value = AadhaarIdViewModel.State.ERROR_NETWORK
                }
            }
        }
    }

    fun getBen(benId: Long) {
        viewModelScope.launch {
            val beneficiary = benRepo.getBenFromId(benId)
            if (beneficiary == null) {
                _errorMessage.value = "Beneficiary not found"
                return@launch
            }
            beneficiary.let {
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