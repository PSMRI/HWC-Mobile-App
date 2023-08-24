package org.piramalswasthya.cho.ui.home

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.UserRepo

import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo

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
) : ViewModel() {

    init {
        viewModelScope.launch{
            try {
                malMasterDataRepo.getMasterDataForNurse()
                languageRepo.saveResponseToCacheLang()
                visitReasonsAndCategoriesRepo.saveVisitReasonResponseToCache()
                visitReasonsAndCategoriesRepo.saveVisitCategoriesResponseToCache()
                registrarMasterDataRepo.saveGenderMasterResponseToCache()
                registrarMasterDataRepo.saveAgeUnitMasterResponseToCache()
                registrarMasterDataRepo.saveIncomeMasterResponseToCache()
                registrarMasterDataRepo.saveLiteracyStatusServiceResponseToCache()
                registrarMasterDataRepo.saveCommunityMasterResponseToCache()
                registrarMasterDataRepo.saveMaritalStatusServiceResponseToCache()
                registrarMasterDataRepo.saveGovIdEntityMasterResponseToCache()
                registrarMasterDataRepo.saveOtherGovIdEntityMasterResponseToCache()
                registrarMasterDataRepo.saveOccupationMasterResponseToCache()
                registrarMasterDataRepo.saveQualificationMasterResponseToCache()
                registrarMasterDataRepo.saveReligionMasterResponseToCache()
                registrarMasterDataRepo.saveRelationshipMasterResponseToCache()
                vaccineAndDoseTypeRepo.saveVaccineTypeResponseToCache()
                vaccineAndDoseTypeRepo.saveDoseTypeResponseToCache()
            }
            catch (_: Exception){

            }
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