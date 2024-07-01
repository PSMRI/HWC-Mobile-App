package org.piramalswasthya.cho.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.repositories.UserRepo
import javax.inject.Inject

@HiltViewModel
class OfflineSyncViewModel @Inject constructor(
    private val patientDao: PatientDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo
):ViewModel(){

    var patients = emptyList<PatientDisplay>()
    var userRole = ""
    private lateinit var user :UserDomain
    var userName = ""
    init{
        getUserDetails()
        if(preferenceDao.isUserRegistrar()){
            getUnsyncedRegistrarData()
            userRole ="Registrar"
        }
    }

    fun getUserDetails(){
        viewModelScope.launch {
           user = userRepo.getLoggedInUser()!!
            userName = user.userName
        }
    }
  fun  getUnsyncedRegistrarData(){
     _state.postValue(State.FETCHING)
      try{
          viewModelScope.launch {
              patients =  patientDao.getPatientListUnsynced()
             _state.postValue(State.SUCCESS)
          }
      }catch (e:Exception){
          _state.postValue(State.FAILED)
      }
   }

    enum class State {
        IDLE, FETCHING, SUCCESS, FAILED
    }

    companion object {

        private val _state = MutableLiveData(State.IDLE)

        val state: LiveData<State>
            get() = _state

    }
}