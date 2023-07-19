package org.piramalswasthya.cho.ui.abha_id_activity.create_abha_id


import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.*
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateAbhaViewModel @Inject constructor(
    private val abhaIdRepo: AbhaIdRepo, savedStateHandle: SavedStateHandle
) : ViewModel() {
    enum class State {
        IDLE, LOADING, ERROR_NETWORK, ERROR_SERVER, ERROR_INTERNAL, DOWNLOAD_SUCCESS, ABHA_GENERATE_SUCCESS, OTP_GENERATE_SUCCESS, OTP_VERIFY_SUCCESS, DOWNLOAD_ERROR
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    var abha = MutableLiveData<CreateAbhaIdResponse?>(null)

    var hidResponse = MutableLiveData<CreateHIDResponse?>(null)

    private val txnId =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).txnId

    val otpTxnID = MutableLiveData<String?>(null)

    val cardBase64 = MutableLiveData<String>(null)

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    init {
        _state.value = State.LOADING
    }

    fun createHID(benId: Long, benRegId: Long) {
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.createHealthIdWithUid(CreateHealthIdRequest(
                    "", txnId, "", "", "", "", "",
                    "", "", "", "", "", "",
                    "","", 0, "", 34, ""))) {
                is NetworkResult.Success -> {
                    hidResponse.value = result.data
                    if ((benId != 0L) and (benRegId != 0L)) {
                        mapBeneficiary(benId, benRegId, result.data.hID.toString(), result.data.healthIdNumber)
                    } else {
                        _state.value = State.ABHA_GENERATE_SUCCESS
                    }
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    private fun mapBeneficiary(benId: Long, benRegId: Long, healthId: String, healthIdNumber: String?) {
        val req = MapHIDtoBeneficiary(benRegId, benId, healthId, healthIdNumber,34, "")

        viewModelScope.launch {
            when (val result =
                abhaIdRepo.mapHealthIDToBeneficiary(req)) {
                is NetworkResult.Success -> {
                    _state.value = State.ABHA_GENERATE_SUCCESS
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun generateOtp() {
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.generateOtpHid(GenerateOtpHid("AADHAAR_OTP", hidResponse.value?.healthId,
                    hidResponse.value?.healthIdNumber))) {
                is NetworkResult.Success -> {
                    otpTxnID.value = result.data
                    _state.value = State.OTP_GENERATE_SUCCESS
                }
                is NetworkResult.Error -> {
                    if (result.code == 0) {
                        _errorMessage.value = result.message
                        _state.value = State.DOWNLOAD_ERROR
                    } else {
                        _errorMessage.value = result.message
                        _state.value = State.ERROR_SERVER
                    }
                }
                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun verifyOtp(otp: String?) {
        _state.value = State.LOADING
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.verifyOtpAndGenerateHealthCard(ValidateOtpHid(otp,otpTxnID.value,"AADHAAR_OTP"))) {
                is NetworkResult.Success -> {
                    cardBase64.value = result.data
                    _state.value = State.OTP_VERIFY_SUCCESS
                }
                is NetworkResult.Error -> {
                    if (result.code == 0) {
                        _errorMessage.value = result.message
                        _state.value = State.DOWNLOAD_ERROR
                    } else {
                        _errorMessage.value = result.message
                        _state.value = State.ERROR_SERVER
                    }
                }
                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }
}