package org.piramalswasthya.cho.ui.login_activity.cho_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.FingerPrint
import org.piramalswasthya.cho.repositories.UserRepo
import javax.inject.Inject

@HiltViewModel
class ChoLoginViewModel @Inject constructor(private val userRepo: UserRepo): ViewModel() {
    private var _fingerListData: LiveData<List<FingerPrint>> = MutableLiveData()
    val fingerListData : LiveData<List<FingerPrint>>
        get() = _fingerListData
    fun getFingerPrintData(){
        _fingerListData = userRepo.getFPDataFromLocalDB()
    }

//    fun insertAuditData(loginType: String?,
//                        selectedOption: String?,
//                        loginTimeStamp: String?,
//                        logoutTimeStamp: String?,
//                        lat: Double?,
//                        long: Double?,
//                        logoutType: String?,
//                        userName:String){
//        viewModelScope.launch {
//            userRepo.updateLoginStatus(userName)
//            userRepo.setOutreachProgram(loginType, selectedOption, loginTimeStamp, logoutTimeStamp, lat, long, logoutType)
//        }
//    }
}