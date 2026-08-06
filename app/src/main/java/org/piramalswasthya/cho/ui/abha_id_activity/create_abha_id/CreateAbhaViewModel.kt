package org.piramalswasthya.cho.ui.abha_id_activity.create_abha_id


import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.piramalswasthya.cho.model.BenHealthIdDetails
import org.piramalswasthya.cho.network.*
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateAbhaViewModel @Inject constructor(
    private val abhaIdRepo: AbhaIdRepo,
    private val patientRepo: PatientRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    enum class State {
        IDLE, LOADING, ERROR_NETWORK, ERROR_SERVER, ERROR_INTERNAL, DOWNLOAD_SUCCESS, ABHA_GENERATE_SUCCESS, OTP_GENERATE_SUCCESS, OTP_VERIFY_SUCCESS, DOWNLOAD_ERROR
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    var abha = MutableLiveData<CreateAbhaIdResponse?>(null)

    var hidResponse = MutableLiveData<CreateHIDResponse?>(null)

    private val _benMapped = MutableLiveData<String?>(null)
    val benMapped: LiveData<String?>
        get() = _benMapped

    private val txnId =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).txnId

    private val abhaResponse =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).abhaResponse

    private val hID =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).phrAddress

    private val healthIdNumber =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).abhaNumber

    val otpTxnID = MutableLiveData<String?>(null)

    val cardBase64 = MutableLiveData<String>(null)

    val byteImage = MutableLiveData<ResponseBody>(null)

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _abhaResponseLiveData = MutableLiveData<AbhaVerifyAadhaarOtpResponse?>(null)
    val abhaResponseLiveData: LiveData<AbhaVerifyAadhaarOtpResponse?>
        get() = _abhaResponseLiveData

    init {
        _state.value = State.LOADING
        if (abhaResponse!=null){
            val response: AbhaVerifyAadhaarOtpResponse = Gson().fromJson(abhaResponse, AbhaVerifyAadhaarOtpResponse::class.java) ?: AbhaVerifyAadhaarOtpResponse()
            if (response!=null){
                _abhaResponseLiveData.value = response
            }
        }
    }

    fun printAbhaCard() {
        viewModelScope.launch {
            val result = abhaIdRepo.printAbhaCard()
            when (result) {
                is NetworkResult.Success -> {
                    byteImage.value = result.data
                    _state.value = State.DOWNLOAD_SUCCESS
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

    fun mapBeneficiaryToHealthId(benId: Long, benRegId: Long) {
        viewModelScope.launch {
            if ((benId != 0L) || (benRegId != 0L)) {
                mapBeneficiary(
                    benId,
                    if (benRegId != 0L) benRegId else null,
                    hID,
                    healthIdNumber,
                    abhaResponseLiveData.value!!
                )
            } else {
                addHealthIdRecord(abhaResponseLiveData.value!!)
                _state.value = State.ABHA_GENERATE_SUCCESS
            }
        }
    }

    /*  fun createHID(benId: Long, benRegId: Long) {
          viewModelScope.launch {
              when (val result =
                  abhaIdRepo.createHealthIdWithUid(
                      CreateHealthIdRequest(
                          "", txnId, "", "", "", "", "",
                          "", "", "", "", "", "",
                          "", "", 0, "", pref.getLoggedInUser()?.serviceMapId, ""
                      )
                  )) {
                  is NetworkResult.Success -> {
                      hidResponse.value = result.data
                      Timber.d("mapping abha to beneficiary with id $benId")
                      if ((benId != 0L) or (benRegId != 0L)) {
                          mapBeneficiary(
                              benId,
                              if (benRegId != 0L) benRegId else null,
                              result.data.hID.toString(),
                              result.data.healthIdNumber
                          )
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
      }*/

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    private suspend fun mapBeneficiary(
        benId: Long,
        benRegId: Long?,
        healthId: String,
        healthIdNumber: String?,
        response: AbhaVerifyAadhaarOtpResponse
    ) {
        val ben = patientRepo.getBenFromId(benId)
        /*This change is urgent fix as api throwing let response*/
        ben?.let {
            ben.firstName?.let { firstName ->
                _benMapped.value = firstName
            }
            ben.lastName?.let { lastName ->
                _benMapped.value = ben.firstName + " $lastName"
            }
            it.healthIdDetails = BenHealthIdDetails(healthId, healthIdNumber?:"", isNewAbha = response.isNew)
            it.isNewAbha =response.isNew
            patientRepo.updateRecord(ben)
        }
        /**/
        val abhaProfile = ABHAProfile(
            firstName = response.ABHAProfile.firstName,
            middleName = response.ABHAProfile.middleName,
            lastName = response.ABHAProfile.lastName,
            dob = response.ABHAProfile.dob,
            gender = response.ABHAProfile.gender,
            mobile = response.ABHAProfile.mobile,
            email = response.ABHAProfile.email,
            phrAddress = response.ABHAProfile.phrAddress,
            address = response.ABHAProfile.address,
            districtCode = response.ABHAProfile.districtCode,
            stateCode = response.ABHAProfile.stateCode,
            pinCode = response.ABHAProfile.pinCode,
            abhaType = response.ABHAProfile.abhaType,
            stateName = response.ABHAProfile.stateName,
            districtName = response.ABHAProfile.districtName,
            ABHANumber = response.ABHAProfile.ABHANumber,
            abhaStatus = response.ABHAProfile.abhaStatus
        )

        val req = MapHIDtoBeneficiary(
            beneficiaryRegID = benRegId,
            beneficiaryID = benId,
            healthId = healthId,
            healthIdNumber = healthIdNumber,
            providerServiceMapId = 34,
            createdBy = "",
            message =response.message,
            txnId=response.txnId,
            ABHAProfile = abhaProfile,
            isNew = response.isNew
        )

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

    private suspend fun addHealthIdRecord(response: AbhaVerifyAadhaarOtpResponse) {
        val abhaProfile = ABHAProfile(
            firstName = response.ABHAProfile.firstName,
            middleName = response.ABHAProfile.middleName,
            lastName = response.ABHAProfile.lastName,
            dob = response.ABHAProfile.dob,
            gender = response.ABHAProfile.gender,
            mobile = response.ABHAProfile.mobile,
            email = response.ABHAProfile.email,
            phrAddress = response.ABHAProfile.phrAddress,
            address = response.ABHAProfile.address,
            districtCode = response.ABHAProfile.districtCode,
            stateCode = response.ABHAProfile.stateCode,
            pinCode = response.ABHAProfile.pinCode,
            abhaType = response.ABHAProfile.abhaType,
            stateName = response.ABHAProfile.stateName,
            districtName = response.ABHAProfile.districtName,
            ABHANumber = response.ABHAProfile.ABHANumber,
            abhaStatus = response.ABHAProfile.abhaStatus
        )

        val req = AddHealthIdRecord(
            healthId = hID,
            healthIdNumber = healthIdNumber,
            providerServiceMapId = 34,
            createdBy = "",
            message =response.message,
            txnId=response.txnId,
            ABHAProfile = abhaProfile,
            isNew = response.isNew
        )

        viewModelScope.launch {
            when (val result =
                abhaIdRepo.addHealthIdRecord(req)) {
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
                abhaIdRepo.generateOtpHid(
                    GenerateOtpHid(
                        "AADHAAR_OTP", hidResponse.value?.healthId,
                        hidResponse.value?.healthIdNumber
                    )
                )) {
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
                abhaIdRepo.verifyOtpAndGenerateHealthCard(
                    ValidateOtpHid(
                        otp,
                        otpTxnID.value,
                        "AADHAAR_OTP"
                    )
                )) {
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