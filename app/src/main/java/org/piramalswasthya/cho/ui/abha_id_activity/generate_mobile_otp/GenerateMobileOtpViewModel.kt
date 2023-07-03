package org.piramalswasthya.cho.ui.abha_id_activity.generate_mobile_otp

import androidx.lifecycle.*
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.*
import org.piramalswasthya.cho.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import javax.inject.Inject

@HiltViewModel
class GenerateMobileOtpViewModel @Inject constructor(
    private val abhaIdRepo: AbhaIdRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    enum class State {
        IDLE,
        LOADING,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS,
        ABHA_GENERATED_SUCCESS
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    val txnIdFromArgs =
        GenerateMobileOtpFragmentArgs.fromSavedStateHandle(savedStateHandle).txnId

    private var _apiResponse: AbhaCheckAndGenerateMobileOtpResponse? = null
    val apiResponse: AbhaCheckAndGenerateMobileOtpResponse
        get() = _apiResponse!!

    var abha = MutableLiveData<CreateAbhaIdResponse?>(null)

    fun generateOtpClicked(phoneNumber: String) {
        _state.value = State.LOADING
        generateMobileOtp(phoneNumber)
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    private fun generateMobileOtp(phoneNumber: String) {
        viewModelScope.launch {
            val result = abhaIdRepo.checkAndGenerateOtpForMobileNumber(
                AbhaGenerateMobileOtpRequest(
                    phoneNumber,
                    txnIdFromArgs
                )
            )
            when (result) {
                is NetworkResult.Success -> {
                    _apiResponse = result.data
                    _state.value = State.SUCCESS
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
                null, null, null, null, null, null,
                null, txnIdFromArgs
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