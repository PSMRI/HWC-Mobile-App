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

    enum class Abha {
        NONE,
        CREATE,
        SEARCH
    }

    //    val aadhaarVerificationTypeValues = arrayOf("Aadhaar ID", "Fingerprint")
    val aadhaarVerificationTypeValues = arrayOf("Aadhaar No")
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

    private var _abhaMode = MutableLiveData(Abha.NONE)
    val abhaMode: LiveData<Abha>
        get() = _abhaMode

    private var _verificationType = MutableLiveData("OTP")
    val verificationType: LiveData<String>
        get() = _verificationType

    private var _abhaResponse: String? = null
    val abhaResponse: String
        get() = _abhaResponse!!

    private var _txnId: String? = null
    val txnId: String
        get() = _txnId?:""

    private var _otpTxnId: String? = null
    val otpTxnId: String
        get() = _otpTxnId?:""

    private var _mobileNumber: String? = null
    val mobileNumber: String
        get() = _mobileNumber?:""

    private var _selectedAbhaIndex: String? = null
    val selectedAbhaIndex: String
        get() = _selectedAbhaIndex?:""

    private var _aadhaarNumber: String? = null
    val aadhaarNumber: String
        get() = _aadhaarNumber?:""

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _beneficiaryName = MutableLiveData<String?>(null)
    val beneficiaryName: LiveData<String?>
        get() = _beneficiaryName

    private val _navigateToAadhaarConsent = MutableLiveData<Boolean>(false)
    val navigateToAadhaarConsent: LiveData<Boolean>
        get() = _navigateToAadhaarConsent


    private val _consentChecked = MutableLiveData<Boolean>(false)
    val consentChecked: LiveData<Boolean>
        get() = _consentChecked

    private var _otpMobileNumberMessage: String? = null
    val otpMobileNumberMessage: String
        get() = _otpMobileNumberMessage?:""

    var selectedNavToggle:String = "navHostFragmentAadhaarId"

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun setAbha(abha: String) {
        _abhaResponse = abha
    }

    fun setBeneficiaryName(name: String) {
        _beneficiaryName.value = name
    }

    fun setConsentChecked(value: Boolean) {
        _consentChecked.value = value
    }

    fun setState(state: State) {
        _state.value = state
    }

    fun setAbhaMode(abha: Abha) {
        _abhaMode.value = abha
    }

    fun navigateToAadhaarConsent(value: Boolean) {
        _navigateToAadhaarConsent.value = value
    }

    fun setMobileNumber(mobileNumber: String) {
        _mobileNumber = mobileNumber
    }

    fun setAadhaarNumber(aadhaarNumber: String) {
        _aadhaarNumber = aadhaarNumber
    }

    fun setSelectedAbhaIndex(abhaIndex: String) {
        _selectedAbhaIndex = abhaIndex
    }

    fun setOtpTxnId(txnId: String) {
        _otpTxnId = txnId
    }

    fun setTxnId(txnId: String) {
        _txnId = txnId
    }
    fun setOTPMsg(msg: String) {
        _otpMobileNumberMessage = msg
    }


    fun setUserType(userType: String) {
        _userType.value = userType
    }

    fun setVerificationType(verificationType: String) {
        _verificationType.value = verificationType
    }
}
