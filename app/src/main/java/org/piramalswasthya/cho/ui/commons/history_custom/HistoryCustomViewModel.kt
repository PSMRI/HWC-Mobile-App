package org.piramalswasthya.cho.ui.commons.history_custom

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
@HiltViewModel
class HistoryCustomViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository,
    private val userRepo: UserRepo,
    @ApplicationContext private val application : Context
): ViewModel(){
    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser

    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(application.applicationContext)

    private var _boolCall = MutableLiveData(false)
    val boolCall: LiveData<Boolean>
        get() = _boolCall
    fun getLoggedInUserDetails(){
        viewModelScope.launch {
            try {
                _loggedInUser = userRepo.getUserCacheDetails()
                _boolCall.value = true
            } catch (e: Exception){
                Timber.d("Error in calling getLoggedInUserDetails() $e")
                _boolCall.value = false
            }
        }
    }
    fun saveIllnessORSurgeryDetailsInfo(observation: Observation){
        viewModelScope.launch {
            try{
                   var uuid = generateUuid()
                    observation.id = uuid
                    fhirEngine.create(observation)
            } catch (e: Exception){
                Timber.d("Error in Saving Illness Details Informations")
            }
        }
    }
    fun saveMedicationDetailsInfo(medicationRequest: MedicationRequest){
        viewModelScope.launch {
            try{
                var uuid = generateUuid()
                medicationRequest.id = uuid
                fhirEngine.create(medicationRequest)
            } catch (e: Exception){
                Timber.d("Error in Saving Medication Details Informations")
            }
        }
    }
    suspend fun getIllMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getIllnessByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }
    suspend fun getSurgMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getSurgeryByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }
    private fun generateUuid():String{
        return UUID.randomUUID().toString()
    }
    fun resetBool(){
        _boolCall.value = false
    }

}