package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.CreateAbhaIdGovRequest
import org.piramalswasthya.cho.network.CreateAbhaIdResponse
import org.piramalswasthya.cho.network.DistrictCodeResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.StateCodeResponse
import org.piramalswasthya.cho.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class
AadhaarIdViewModel @Inject constructor(
) : ViewModel() {
    enum class State {
        IDLE,
        LOADING,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS,
        STATE_DETAILS_SUCCESS,
        ABHA_GENERATED_SUCCESS
    }

    val aadhaarVerificationTypeValues = arrayOf("Aadhaar ID", "Fingerprint")
    private val _aadhaarVerificationTypes = MutableLiveData(aadhaarVerificationTypeValues[0])
    val aadhaarVerificationTypes: LiveData<String>
        get() = _aadhaarVerificationTypes
    init {
        Timber.d("initialised at ${Date().time}")
    }
    @Inject
    lateinit var abhaIdRepo: AbhaIdRepo


    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private var _userType = MutableLiveData("ASHA")
    val userType: LiveData<String>
        get() = _userType

    private var _verificationType = MutableLiveData("OTP")
    val verificationType: LiveData<String>
        get() = _verificationType

    private var _abhaResponse: String? = null

    var abha = MutableLiveData<CreateAbhaIdResponse?>(null)

    val abhaResponse: String
        get() = _abhaResponse!!

    private var _txnId: String? = null
    val txnId: String
        get() = _txnId!!

    private var _mobileNumber: String? = null
    val mobileNumber: String
        get() = _mobileNumber!!

    var activeState: StateCodeResponse? = null

    var activeDistrict: DistrictCodeResponse? = null

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun setAbha(abha: String) {
        _abhaResponse = abha
    }

    fun setState(s:StateCodeResponse){
        activeState = s
    }

    fun setD(s:DistrictCodeResponse){
        activeDistrict = s
    }

    fun setState(state: State) {
        _state.value = state
    }

    fun generateAbhaCard(aadhaarNumber: String, fullName: String, dateOfBirth: String, gender: String): String {
        _state.value = AadhaarIdViewModel.State.LOADING
        viewModelScope.launch {

            val result: NetworkResult<CreateAbhaIdResponse>?

            val createRequest = CreateAbhaIdGovRequest(
                aadhaarNumber.toLong(),
                "GoK HWC",
                true,
                dateOfBirth,
                gender,
                fullName,
                activeState?.code?.toInt()?: 0,
                activeDistrict?.code?.toInt()?: 0
            )
            result = abhaIdRepo.generateAbhaIdGov(createRequest)


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
        return Gson().toJson(abha.value)
    }

    fun setMobileNumber(mobileNumber: String) {
        _mobileNumber = mobileNumber
    }

    fun setTxnId(txnId: String) {
        _txnId = txnId
    }

    fun setUserType(userType: String) {
        _userType.value = userType
    }

    fun setVerificationType(verificationType: String) {
        _verificationType.value = verificationType
    }
}
