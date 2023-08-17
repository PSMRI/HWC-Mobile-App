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
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VaccineType
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
@HiltViewModel
class HistoryCustomViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository,
    private val vaccineAndDoseTypeRepo: VaccineAndDoseTypeRepo,
    private val userRepo: UserRepo,
    @ApplicationContext private val application : Context
): ViewModel(){

    private var _vaccinationTypeDropdown:LiveData<List<VaccineType>>
    val vaccinationTypeDropdown:LiveData<List<VaccineType>>
        get() = _vaccinationTypeDropdown

    private var _doseTypeDropdown:LiveData<List<DoseType>>
    val doseTypeDropdown:LiveData<List<DoseType>>
        get() = _doseTypeDropdown

    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser

    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(application.applicationContext)

    private var _boolCall = MutableLiveData(false)
    val boolCall: LiveData<Boolean>
        get() = _boolCall

    init {
        _doseTypeDropdown = MutableLiveData()
        _vaccinationTypeDropdown = MutableLiveData()
        getDoseTypeDropdown()
        getVaccinationTypeDropdown()
    }
     fun getDoseTypeDropdown(){
        try {
            _doseTypeDropdown = vaccineAndDoseTypeRepo.getDoseTypeCachedResponse()
        }
        catch (e:Exception){
            Timber.d("Error in getDoseType $e")
        }
    }
    fun getVaccinationTypeDropdown(){
        try {
            _vaccinationTypeDropdown = vaccineAndDoseTypeRepo.getVaccineTypeCachedResponse()
            Log.d("Arta","$_vaccinationTypeDropdown.")
        }
        catch (e:Exception){
            Timber.d("Error in getVaccinationType $e")
        }
    }
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

    fun saveIllnessDetailsInfo(observation: List<Observation>){
        viewModelScope.launch {
            try{
                observation.forEach { obs ->
                   var uuid = generateUuid()
                    obs.id = uuid
                    fhirEngine.create(obs)
                    var getobs = fhirEngine.get(ResourceType.Observation,uuid)
                }
            } catch (e: Exception){
                Timber.d("Error in Saving Visit Details Informations")
            }
        }
    }
    fun saveCovidDetailsInfo(immunization: Immunization){
        viewModelScope.launch {
            try{
                    var uuid = generateUuid()
                    immunization.id = uuid
                    fhirEngine.create(immunization)
            } catch (e: Exception){
                Timber.d("Error in Saving Visit Details Informations")
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
    suspend fun getDoseTypeMap(): Map<Int, String> {
        return try {
            vaccineAndDoseTypeRepo.getDoseTypeByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }
    suspend fun getVaccineTypeMap(): Map<Int, String> {
        return try {
            vaccineAndDoseTypeRepo.getVaccineTypeByNameMap()
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