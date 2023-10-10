package org.piramalswasthya.cho.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.UserRepo

import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.work.WorkerUtils

import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val database: InAppDb,
    private val pref: PreferenceDao,
    private val userRepo: UserRepo,
    private val userDao: UserDao,
    private val registrarMasterDataRepo: RegistrarMasterDataRepo,
    private val languageRepo: LanguageRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vaccineAndDoseTypeRepo: VaccineAndDoseTypeRepo,
    private val malMasterDataRepo: MaleMasterDataRepository,
    private val doctorMaleMasterDataRepo: DoctorMasterDataMaleRepo,
    private val dataLoadFlagManager: DataLoadFlagManager
) : ViewModel() {

    companion object{
        private val _searchBool = MutableLiveData<Boolean?>()
        val searchBool: LiveData<Boolean?>
            get() = _searchBool
        fun setSearchBool(){
            _searchBool.value = true
        }
        fun resetSearchBool(){
            _searchBool.value = false
        }
    }

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }
    private var providerServiceMapID: Int = -1
    private var vanID : Int =-1
    private var facilityID : Int =-1
    private var visitCategoryID: Int = 6
    var gender : String = "Male"

    private val _state = MutableLiveData(State.IDLE)

    val state: LiveData<State>
        get() = _state

    fun init(context: Context){
        viewModelScope.launch {
             extracted(context)
        }
    }

    private suspend fun extracted(context: Context) {
        try {
            _state.postValue(State.SAVING)
            if (dataLoadFlagManager.isDataLoaded())
                WorkerUtils.triggerAmritSyncWorker(context)
            WorkerUtils.pushAuditDetailsWorker(context)
            registrarMasterDataRepo.saveGenderMasterResponseToCache()
            registrarMasterDataRepo.saveAgeUnitMasterResponseToCache()
            registrarMasterDataRepo.saveMaritalStatusServiceResponseToCache()
            registrarMasterDataRepo.saveCommunityMasterResponseToCache()
            registrarMasterDataRepo.saveReligionMasterResponseToCache()
            languageRepo.saveResponseToCacheLang()
            visitReasonsAndCategoriesRepo.saveVisitReasonResponseToCache()
            visitReasonsAndCategoriesRepo.saveVisitCategoriesResponseToCache()
            registrarMasterDataRepo.saveIncomeMasterResponseToCache()
            registrarMasterDataRepo.saveLiteracyStatusServiceResponseToCache()
            registrarMasterDataRepo.saveGovIdEntityMasterResponseToCache()
            registrarMasterDataRepo.saveOtherGovIdEntityMasterResponseToCache()
            registrarMasterDataRepo.saveOccupationMasterResponseToCache()
            registrarMasterDataRepo.saveQualificationMasterResponseToCache()
            registrarMasterDataRepo.saveRelationshipMasterResponseToCache()
            vaccineAndDoseTypeRepo.saveVaccineTypeResponseToCache()
            vaccineAndDoseTypeRepo.saveDoseTypeResponseToCache()
            vanID = userDao.getLoggedInUserVanID()
            facilityID = userDao.getLoggedInUserFacilityID()
            providerServiceMapID = userDao.getLoggedInUserProviderServiceMapId()
            doctorMaleMasterDataRepo.getDoctorMasterMaleData(visitCategoryID,providerServiceMapID,gender,facilityID,vanID)
            malMasterDataRepo.getMasterDataForNurse(visitCategoryID, providerServiceMapID,gender)
            if (!dataLoadFlagManager.isDataLoaded())
                WorkerUtils.triggerAmritSyncWorker(context)
            dataLoadFlagManager.setDataLoaded(true)
            _state.postValue(State.SAVE_SUCCESS)
        } catch (_e: Exception) {
            _state.postValue(State.SAVE_FAILED)
        }
    }


    val scope: CoroutineScope
        get() = viewModelScope
    private var _unprocessedRecords: Int = 0
    val unprocessedRecords: Int
        get() = _unprocessedRecords



    private val _navigateToLoginPage = MutableLiveData(false)
    val navigateToLoginPage: MutableLiveData<Boolean>
        get() = _navigateToLoginPage


    fun logout() {
        viewModelScope.launch {
            _navigateToLoginPage.value = true
            userDao.resetAllUsersLoggedInState()
        }
    }

    fun navigateToLoginPageComplete() {
        _navigateToLoginPage.value = false
    }


}
