package org.piramalswasthya.cho.ui.login_activity.cho_login.outreach

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.UserAuthRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OutreachViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val languageRepo: LanguageRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val registrarMasterDataRepo: RegistrarMasterDataRepo,
    private val stateMasterRepo: StateMasterRepo,
    private val userAuthRepo: UserAuthRepo,
    private val pref: PreferenceDao
) : ViewModel() {
    // TODO: Implement the ViewModel

    enum class State {
        IDLE,
        LOADING,
        ERROR_INPUT,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    fun loginInClicked() {
        _state.value = State.LOADING
    }
    init {
        Timber.tag("initMethod").d("initMethod inside")
        viewModelScope.launch {
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
            registrarMasterDataRepo.saveOccupationMasterResponseToCache()
            registrarMasterDataRepo.saveRelationshipMasterResponseToCache()
            stateMasterRepo.saveStateMasterResponseToCache()
        }
    }
    fun dummyAuthUser(username: String, password: String) {
        viewModelScope.launch {
            userAuthRepo.dummyAuth(username, password,)
        }
    }



    fun authUser(username: String, password: String, state: String) {
        viewModelScope.launch {
            //Temporary Placement - need to move to  assets and load from there.
            _state.value = userRepo.authenticateUser(username, password, state)

        }
    }

}