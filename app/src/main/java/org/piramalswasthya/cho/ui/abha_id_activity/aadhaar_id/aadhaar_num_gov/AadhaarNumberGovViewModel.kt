package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.aadhaar_num_gov


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.*
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import org.piramalswasthya.cho.network.interceptors.TokenInsertAbhaInterceptor
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AadhaarNumberGovViewModel @Inject constructor(
    private var abhaIdRepo: AbhaIdRepo
) : ViewModel() {

    private val _state = MutableLiveData(AadhaarIdViewModel.State.IDLE)
    val state: LiveData<AadhaarIdViewModel.State>
        get() = _state

    private var _stateCodes = MutableLiveData<List<StateCodeResponse>?>(null)
    val stateCodes: LiveData<List<StateCodeResponse>?>
        get() = _stateCodes

    var activeState: StateCodeResponse? = null

    var activeDistrict: DistrictCodeResponse? = null

    var aadhaarNumber: Long = 0

    var fullName: String = ""

    var gender: String = ""

    var dateOfBirth: String = ""

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    var abha = MutableLiveData<CreateAbhaIdResponse?>(null)

    init {
        getStates()
    }

    /**
     * getting state and district codes from api's
     */
    private fun getStates() {
        _state.value = AadhaarIdViewModel.State.LOADING
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.getStateAndDistricts()) {
                is NetworkResult.Success -> {
                    _stateCodes.value = result.data
                    _state.value = AadhaarIdViewModel.State.STATE_DETAILS_SUCCESS
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

    fun generateAbhaCard(aadhaarNumber: String, fullName: String, dateOfBirth: String, gender: String): String {
        _state.value = AadhaarIdViewModel.State.LOADING
        viewModelScope.launch {

            val result: NetworkResult<CreateAbhaIdResponse>?

            val createRequest = CreateAbhaIdGovRequest(
                aadhaarNumber.toLong(),
                "healthid api",
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
                    _state.value = AadhaarIdViewModel.State.ABHA_GENERATED_SUCCESS
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = AadhaarIdViewModel.State.ERROR_SERVER
                }
                is NetworkResult.NetworkError -> {
                    _state.value = AadhaarIdViewModel.State.ERROR_NETWORK
                }
            }

        }
        return Gson().toJson(abha.value)
    }
}