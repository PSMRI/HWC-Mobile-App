package org.piramalswasthya.cho.ui.login_activity.cho_login.outreach

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.OutreachDropdownList
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.OutreachRepo
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.UserAuthRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OutreachViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val outreachRepo: OutreachRepo,
    private val languageRepo: LanguageRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val registrarMasterDataRepo: RegistrarMasterDataRepo,
    private val stateMasterRepo: StateMasterRepo,
    private val vaccineAndDoseTypeRepo: VaccineAndDoseTypeRepo,
    private val userAuthRepo: UserAuthRepo,
    private val userDao: UserDao,
    private val pref: PreferenceDao
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        SAVING,
        ERROR_INPUT,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS
    }
    private var _outreachList: LiveData<List<OutreachDropdownList>>
    val outreachList: LiveData<List<OutreachDropdownList>>
        get() = _outreachList

     val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    fun loginInClicked() {
        _state.value = State.LOADING
    }


    init {
        _outreachList= MutableLiveData()
        getOutreach()
    }

fun getOutreach(){
    viewModelScope.launch {
        try {
            _outreachList = outreachRepo.getAllOutreachDropdownList()

        } catch (e: java.lang.Exception) {
            Timber.d("Error in getFormMaster $e")
        }
    }
}
    suspend fun getReferNameTypeMap(): Map<Int, String> {
        return try {
            outreachRepo.getONameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }
    fun authUser(
        username: String,
        password: String,
        loginType: String,
        lat: Double?,
        long: Double?,
        context: Context
    ) {
        _state.value = State.SAVING
        viewModelScope.launch {
            _state.value = userRepo.authenticateUser(
                username,
                password,
                loginType,
                lat,
                long,
                context)

        }
    }
    suspend fun setOutreachDetails(
        loginType: String?,
        selectedOption: String?,
        loginTimeStamp: String?,
        logoutTimeStamp: String?,
        lat: Double?,
        long: Double?,
        logoutType: String?,
        imageString : String?,
        isOutOfReach : Boolean?
    ){
        userRepo.setOutreachProgram(
            loginType,
            selectedOption,
            loginTimeStamp,
            logoutTimeStamp,
            lat,
            long,
            logoutType,
            imageString,
            isOutOfReach
        )
    }
    fun rememberUser(username: String,password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pref.registerLoginCred(username,password)
            }
        }
    }
    fun fetchRememberedPassword(): String? =
        pref.getRememberedPassword()

    fun forgetUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pref.deleteLoginCred()
            }
        }
    }
    fun resetState() {
        _state.value = State.IDLE
    }

}