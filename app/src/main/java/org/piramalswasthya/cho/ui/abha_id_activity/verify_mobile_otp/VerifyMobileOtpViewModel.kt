package org.piramalswasthya.cho.ui.abha_id_activity.verify_mobile_otp


import androidx.lifecycle.*
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.*
import org.piramalswasthya.cho.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import org.piramalswasthya.cho.ui.abha_id_activity.create_abha_id.CreateAbhaViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyMobileOtpViewModel @Inject constructor(
    private val abhaIdRepo: AbhaIdRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    enum class State {
        IDLE,
        LOADING,
        ERROR_SERVER,
        ERROR_NETWORK,
        OTP_VERIFY_SUCCESS,
        OTP_GENERATED_SUCCESS,
        ABHA_GENERATED_SUCCESS
    }

    private val _state = MutableLiveData<State>(State.IDLE)
    val state: LiveData<State>
        get() = _state


    private var txnIdFromArgs =
        VerifyMobileOtpFragmentArgs.fromSavedStateHandle(savedStateHandle).txnId
    private val phoneNumberFromArgs =
        VerifyMobileOtpFragmentArgs.fromSavedStateHandle(savedStateHandle).phoneNum

    private var _txnId: String? = null
    val txnID: String
        get() = _txnId!!

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    var abha = MutableLiveData<CreateAbhaIdResponse?>(null)

    fun verifyOtpClicked(otp: String) {
        _state.value = State.LOADING
        verifyMobileOtp(otp)
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    private fun verifyMobileOtp(otp: String) {
        viewModelScope.launch {
            val result = abhaIdRepo.verifyOtpForMobileNumber(
                AbhaVerifyMobileOtpRequest(
                    otp,
                    txnIdFromArgs
                )
            )
            when (result) {
                is NetworkResult.Success -> {
                    _txnId = result.data.txnId
                    _state.value = State.OTP_VERIFY_SUCCESS
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                is NetworkResult.NetworkError -> {
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun resendOtp() {
        _state.value = State.LOADING
        viewModelScope.launch {
            val result = abhaIdRepo.checkAndGenerateOtpForMobileNumber(
                AbhaGenerateMobileOtpRequest(
                    phoneNumberFromArgs,
                    txnIdFromArgs
                )
            )
            when (result) {
                is NetworkResult.Success -> {
                    txnIdFromArgs = result.data.txnId
                    _txnId = result.data.txnId
                    _state.value = State.OTP_GENERATED_SUCCESS
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                is NetworkResult.NetworkError -> {
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun generateAbhaCard() {
        viewModelScope.launch {

            val result: NetworkResult<CreateAbhaIdResponse>?

            val createRequest = CreateAbhaIdRequest(
                null, null, null, null, null, null, null, _txnId.toString()
            )
            result = abhaIdRepo.generateAbhaId(createRequest)


            when (result) {
                is NetworkResult.Success -> {
                    TokenInsertAbhaInterceptor.setXToken(result.data.token)
                    abha.value = result.data
                    _state.value = State.ABHA_GENERATED_SUCCESS
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                is NetworkResult.NetworkError -> {
                    _state.value = State.ERROR_NETWORK
                }
            }

        }
    }
}