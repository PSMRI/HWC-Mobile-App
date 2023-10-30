package org.piramalswasthya.cho.ui.commons.history_custom

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.model.AlcoholDropdown
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.TobaccoDropdown
import org.piramalswasthya.cho.model.AssociateAilmentsHistory
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.model.CovidVaccinationStatusHistory
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PastSurgeryHistory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.TobaccoAlcoholHistory
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

    private var _tobaccoDropdown: LiveData<List<TobaccoDropdown>>
    val tobaccoDropdown: LiveData<List<TobaccoDropdown>>
        get() = _tobaccoDropdown

    private var _alcoholDropdown: LiveData<List<AlcoholDropdown>>

    val alcoholDropdown: LiveData<List<AlcoholDropdown>>
        get() = _alcoholDropdown

    private var _vaccinationTypeDropdown:LiveData<List<VaccineType>>
    val vaccinationTypeDropdown:LiveData<List<VaccineType>>
        get() = _vaccinationTypeDropdown

    private var _doseTypeDropdown:LiveData<List<DoseType>>
    val doseTypeDropdown:LiveData<List<DoseType>>
        get() = _doseTypeDropdown

    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser

    private var _boolCall = MutableLiveData(false)
    val boolCall: LiveData<Boolean>
        get() = _boolCall

    init {
        _doseTypeDropdown = MutableLiveData()
        _vaccinationTypeDropdown = MutableLiveData()
        _tobaccoDropdown = MutableLiveData()
        _alcoholDropdown = MutableLiveData()
        getAlcoholDropdown()
        getTobaccoDropdown()
        getDoseTypeDropdown()
        getVaccinationTypeDropdown()
    }
    private fun getTobaccoDropdown(){
        try{
            _tobaccoDropdown  = maleMasterDataRepository.getAllTobaccoDropdown()

        } catch (e: java.lang.Exception){
            Timber.d("Error in getTobaccoList() $e")
        }
    }
     fun getDoseTypeDropdown(){
        try {
            _doseTypeDropdown = vaccineAndDoseTypeRepo.getDoseTypeCachedResponse()
        }
        catch (e:Exception){
            Timber.d("Error in getDoseType $e")
        }
    }
    private fun getAlcoholDropdown(){
        try{
            _alcoholDropdown  = maleMasterDataRepository.getAllAlcoholDropdown()

        } catch (e: java.lang.Exception){
            Timber.d("Error in getAlcoholList() $e")
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
    fun savePastIllnessHistoryToCache(pastIllnessHistory: PastIllnessHistory) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    historyRepo.savePastIllnessHistoryToCatche(pastIllnessHistory)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving Past Illness history: $e")
            }
        }
    }
    fun savePastSurgeryHistoryToCache(pastSurgeryHistory: PastSurgeryHistory) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    historyRepo.savePastSurgeryHistoryToCatche(pastSurgeryHistory)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving Past Surgery history: $e")
            }
        }
    }
    fun saveCovidVaccinationStatusHistoryToCache(covidVaccinationStatusHistory: CovidVaccinationStatusHistory) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    historyRepo.saveCovidVaccinationStatusHistoryToCatche(covidVaccinationStatusHistory)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving Covid Vaccination Status history: $e")
            }
        }
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

    fun saveAssociateAilmentsHistoryToCache(associateAilmentsHistory: AssociateAilmentsHistory) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    historyRepo.saveAssociateAilmentsHistoryToCatche(associateAilmentsHistory)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving Medication history: $e")
            }
        }
    }

    fun saveTobAndAlcHistoryToCache(tobaccoAlcoholHistory: TobaccoAlcoholHistory) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    historyRepo.saveTobAndAlcHistoryToCatche(tobaccoAlcoholHistory)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving Tob and Alc history: $e")
            }
        }
    }

    fun resetBool(){
        _boolCall.value = false
    }

}