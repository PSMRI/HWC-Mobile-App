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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Immunization
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.MedicationStatement
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VaccineType
import org.piramalswasthya.cho.repositories.HistoryRepo
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
    private val historyRepo: HistoryRepo,
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
    fun saveMedicationDetailsInfo(medicationStatement: MedicationStatement){
        viewModelScope.launch {
            try{
                var uuid = generateUuid()
                medicationStatement.id = uuid
                fhirEngine.create(medicationStatement)
            } catch (e: Exception){
                Timber.d("Error in Saving Medication Details Informations")
                Timber.d("Error in Saving Illness and Surgery Details Informations")
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
                Timber.d("Error in Saving Covid Details Informations")
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
    fun saveMedicationHistoryToCache(medicationHistory: MedicationHistory) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    historyRepo.saveMedicationHistoryToCatche(medicationHistory)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving Medication history: $e")
            }
        }
    }

    fun resetBool(){
        _boolCall.value = false
    }

}