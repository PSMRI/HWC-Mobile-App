package org.piramalswasthya.cho.ui.register_patient_activity.patient_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.MaritalStatusMaster
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.ui.register_patient_activity.location_details.LocationViewModel
import org.piramalswasthya.cho.utils.AgeUnitEnum
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PatientDetailsViewModel @Inject constructor(
    private val database: InAppDb,
    private val pref: PreferenceDao,
    private val userRepo: UserRepo,
    private val userDao: UserDao,
    private val registrarMasterDataRepo: RegistrarMasterDataRepo,
    private val languageRepo: LanguageRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vaccineAndDoseTypeRepo: VaccineAndDoseTypeRepo,
    private val malMasterDataRepo: MaleMasterDataRepository
) : ViewModel() {

    enum class NetworkState {
        IDLE,
        LOADING,
        SUCCESS,
        FAILURE
    }

    private val _ageUnit = MutableLiveData(NetworkState.IDLE)
    val ageUnit: LiveData<NetworkState>
        get() = _ageUnit

    private val _maritalStatus = MutableLiveData(NetworkState.IDLE)
    val maritalStatus: LiveData<NetworkState>
        get() = _maritalStatus

    private val _genderMaster = MutableLiveData(NetworkState.IDLE)
    val genderMaster: LiveData<NetworkState>
        get() = _genderMaster

    var ageUnitMap = mutableMapOf<AgeUnitEnum, AgeUnit>();
    var ageUnitEnumMap = mutableMapOf<AgeUnit, AgeUnitEnum>();
    var ageUnitList : List<AgeUnit> = mutableListOf()
    var maritalStatusList : List<MaritalStatusMaster> = mutableListOf()
    var genderMasterList : List<GenderMaster> = mutableListOf()

    var selectedAgeUnitEnum: AgeUnitEnum?  = null
    var enteredAge: Int?  = null
    var selectedDateOfBirth: Date?  = null
    var selectedAgeUnit : AgeUnit? = null;
    var selectedMaritalStatus : MaritalStatusMaster? = null;
    var selectedGenderMaster : GenderMaster? = null;

    init {
        viewModelScope.launch {
            fetchAgeUnits()
            fetchMaritalStatus()
            fetchGenderMaster()
        }
    }

    suspend fun fetchAgeUnits(){
        _ageUnit.value = NetworkState.LOADING
        try {
            ageUnitList = registrarMasterDataRepo.getAgeUnitMasterCachedResponse()
            _ageUnit.value = NetworkState.SUCCESS
            setAgeUnitMap()
        }
        catch (_: Exception){
            _ageUnit.value = NetworkState.FAILURE
        }
    }

    fun setAgeUnitMap(){
        ageUnitList.forEach {
            when(it.name.first().lowercaseChar()){
                'd' -> {
                    ageUnitMap[AgeUnitEnum.DAYS] = it
                    ageUnitEnumMap[it] = AgeUnitEnum.DAYS
                }
                'w' -> {
                    ageUnitMap[AgeUnitEnum.WEEKS] = it
                    ageUnitEnumMap[it] = AgeUnitEnum.WEEKS
                }
                'm' -> {
                    ageUnitMap[AgeUnitEnum.MONTHS] = it
                    ageUnitEnumMap[it] = AgeUnitEnum.MONTHS
                }
                'y' -> {
                    ageUnitMap[AgeUnitEnum.YEARS] = it
                    ageUnitEnumMap[it] = AgeUnitEnum.YEARS
                }
            }
        }
    }

    suspend fun fetchMaritalStatus(){
        _maritalStatus.value = NetworkState.LOADING
        try {
            maritalStatusList = registrarMasterDataRepo.getMaritalStatusMasterCachedResponse()
            _maritalStatus.value = NetworkState.SUCCESS
        }
        catch (_: Exception){
            _maritalStatus.value = NetworkState.FAILURE
        }
    }

    suspend fun fetchGenderMaster(){
        _genderMaster.value = NetworkState.LOADING
        try {
            genderMasterList = registrarMasterDataRepo.getGenderMasterCachedResponse()
            _genderMaster.value = NetworkState.SUCCESS
        }
        catch (_: Exception){
            _genderMaster.value = NetworkState.FAILURE
        }
    }

}