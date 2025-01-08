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
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.VillageLocationData
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.utils.AgeUnitEnum
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PatientDetailsViewModel @Inject constructor(
    private val database: InAppDb,
    private val prefDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val userDao: UserDao,
    private val patientRepo: PatientRepo,
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

    private val _isClickedSS=MutableLiveData<Boolean>(false)

    val isClickedSS: MutableLiveData<Boolean>
        get() = _isClickedSS

    private val _firstNameVal=MutableLiveData<Boolean>(false)
    val firstNameVal: MutableLiveData<Boolean>
        get() = _firstNameVal

    private val _lastNameVal=MutableLiveData<Boolean>(false)
    val lastNameVal: MutableLiveData<Boolean>
        get() = _lastNameVal

    private val _dobVal=MutableLiveData<Boolean>(false)
    val dobVal: MutableLiveData<Boolean>
        get() = _dobVal

    private val _ageYearsVal =MutableLiveData<Boolean>(false)
    val ageYearsVal: MutableLiveData<Boolean>
        get() = _ageYearsVal

    private val _ageMonthsVal =MutableLiveData<Boolean>(false)
    val ageMonthsVal: MutableLiveData<Boolean>
        get() = _ageMonthsVal

    private val _ageWeeksVal =MutableLiveData<Boolean>(false)
    val ageWeeksVal: MutableLiveData<Boolean>
        get() = _ageWeeksVal

    private val _ageDaysVal =MutableLiveData<Boolean>(false)
    val ageDaysVal: MutableLiveData<Boolean>
        get() = _ageDaysVal

    private val _ageVal =MutableLiveData<Boolean>(false)
    val ageVal: MutableLiveData<Boolean>
        get() = _ageVal

    private val _ageInUnitVal= MutableLiveData<Boolean>(false)
    val ageInUnitVal:MutableLiveData<Boolean>
        get() = _ageInUnitVal

    private val _ageGreaterThan11 = MutableLiveData<Boolean>(false)

    val ageGreaterThan11:MutableLiveData<Boolean>
        get() = _ageGreaterThan11

    private val _maritalStatusVal=MutableLiveData<Boolean>(false)
    val maritalStatusVal: MutableLiveData<Boolean>
        get() = _maritalStatusVal

    private val _spouseNameVal =MutableLiveData<Boolean>(false)
    val spouseNameVal: MutableLiveData<Boolean>
        get() = _spouseNameVal

    private val _relationStatusVal=MutableLiveData<Boolean>(false)
    val relationStatusVal: MutableLiveData<Boolean>
        get() = _relationStatusVal

    private val _ageAtMarraigeVal=MutableLiveData<Boolean>(false)
    val ageAtMarraigeVal: MutableLiveData<Boolean>
        get() = _ageAtMarraigeVal

    private val _phoneN=MutableLiveData<PhoneNumberValidation>()
    val phoneN: MutableLiveData<PhoneNumberValidation>
        get() = _phoneN

    private val _genderVal=MutableLiveData<Boolean>(false)
    val genderVal: MutableLiveData<Boolean>
        get() = _genderVal
    private val _villageBoolVal=MutableLiveData<Boolean>(false)
    val villageBoolVal: MutableLiveData<Boolean>
        get() = _villageBoolVal

    private val _ageUnit = MutableLiveData(NetworkState.IDLE)
    val ageUnit: LiveData<NetworkState>
        get() = _ageUnit
    private val _villageVal = MutableLiveData(NetworkState.IDLE)
    val villageVal: LiveData<NetworkState>
        get() = _villageVal

    private val _maritalStatus = MutableLiveData(NetworkState.IDLE)
    val maritalStatus: MutableLiveData<NetworkState>
        get() = _maritalStatus

    private val _relationStatus = MutableLiveData(NetworkState.IDLE)
    val relationStatus: MutableLiveData<NetworkState>
        get() = _relationStatus

    private val _genderMaster = MutableLiveData(NetworkState.IDLE)
    val genderMaster: MutableLiveData<NetworkState>
        get() = _genderMaster

    private val _isDataSaved = MutableLiveData(false)
    val isDataSaved: MutableLiveData<Boolean>
        get() = _isDataSaved

    var ageUnitMap = mutableMapOf<AgeUnitEnum, AgeUnit>();
    var ageUnitEnumMap = mutableMapOf<AgeUnit, AgeUnitEnum>();
    var ageUnitList : List<AgeUnit> = mutableListOf()
    var villageList  = mutableListOf<VillageLocationData>()
    var villageListFilter  = mutableListOf<VillageLocationData>()
    var maritalStatusList : List<MaritalStatusMaster> = mutableListOf()
    var relationStatusList : List<MaritalStatusMaster> = mutableListOf()
    var genderMasterList : List<GenderMaster> = mutableListOf()

    var selectedAgeUnitEnum: AgeUnitEnum? = AgeUnitEnum.YEARS
    var enteredAge: Int?  = null
    var enteredAgeYears: Int?  = 0
    var enteredAgeMonths: Int?  = 0
    var enteredAgeWeeks: Int?  = 0
    var enteredAgeDays: Int?  = 0
    var selectedDateOfBirth: Date?  = null
    var selectedAgeUnit : AgeUnit? = AgeUnit(3,"Years");
    var selectedMaritalStatus : MaritalStatusMaster? = null;
    var selectedGenderMaster : GenderMaster? = null;
    var selectedVillage : VillageLocationData? = null;
    lateinit var benVisitInfo: PatientDisplayWithVisitInfo

    init {
        viewModelScope.launch {
            fetchAgeUnits()
            fetchMaritalStatus()
            fetchGenderMaster()
            fetchRelationStatus()
            fetchVillages()
        }
    }
   fun setDob(boolean: Boolean){
       _dobVal.value = boolean
   }
    fun setFirstName(boolean: Boolean){
        _firstNameVal.value = boolean
    }
    fun setLastName(boolean: Boolean){
        _lastNameVal.value = boolean
    }
    fun setAge(boolean: Boolean){
        _ageVal.value = boolean
    }
    fun setAgeUnit(boolean: Boolean){
        _ageInUnitVal.value = boolean
    }
    fun setPhoneN(boolean: Boolean, string: String){
        val phoneNumberValidation = PhoneNumberValidation(boolean, string)
         _phoneN.value = phoneNumberValidation
    }
    fun setGender(boolean: Boolean){
        _genderVal.value = boolean
    }
    fun setRelation(boolean: Boolean){
        _relationStatusVal.value = boolean
    }
    fun setVillageBool(boolean: Boolean){
        _villageBoolVal.value = boolean
    }   fun setMarital(boolean: Boolean){
        _maritalStatusVal.value = boolean
    }   fun setMaritalAge(boolean: Boolean){
        _ageAtMarraigeVal.value = boolean
    }   fun setSpouse(boolean: Boolean){
        _spouseNameVal.value = boolean
    }
       fun setAgeGreaterThan11(boolean: Boolean){
           _ageGreaterThan11.value = boolean
       }
     fun setIsClickedSS(boolean: Boolean){
         _isClickedSS.value = boolean
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

    private fun fetchVillages(){
        _villageVal.value = NetworkState.LOADING
        try {
            villageList.addAll(prefDao.getUserLocationData()?.villageList!!)
            villageListFilter.addAll(prefDao.getUserLocationData()?.villageList!!)
            _villageVal.value = NetworkState.SUCCESS
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

    suspend fun fetchRelationStatus(){
        _relationStatus.value = NetworkState.LOADING
        try {
            maritalStatusList = registrarMasterDataRepo.getMaritalStatusMasterCachedResponse()
            _relationStatus.value = NetworkState.SUCCESS
        }
        catch (_: Exception){
            _relationStatus.value = NetworkState.FAILURE
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
    fun insertPatient(patient: Patient){
        viewModelScope.launch {
            patientRepo.insertPatient(patient)
            benVisitInfo = patientRepo.getPatientDisplayListForNurseByPatient(patient.patientID)
            _isDataSaved.value = true
        }
    }

}