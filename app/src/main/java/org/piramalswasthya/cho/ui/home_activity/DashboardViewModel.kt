package org.piramalswasthya.cho.ui.home_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao

import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val database: InAppDb,
    private var benFlowDao: BenFlowDao,
    private val pref: PreferenceDao,
) : ViewModel() {

    init{}

    private val _maleCountLiveData : MutableLiveData<Int?> = MutableLiveData(0)
    val maleCountLiveData: LiveData<Int?>
        get() = _maleCountLiveData
    private val _femaleCountLiveData : MutableLiveData<Int?> = MutableLiveData(0)
    val femaleCountLiveData: LiveData<Int?>
        get() = _femaleCountLiveData
    private val _totalCountLiveData : MutableLiveData<Int?> = MutableLiveData(0)
    val totalCountLiveData: LiveData<Int?>
        get() = _totalCountLiveData

//     fun getMaleCount(genderID : Int, param:String){
//       _maleCountLiveData.value =  benFlowDao.getOpdCount(genderID, param).value
//    }
//     fun getFemaleCount(genderID : Int, param:String){
//        _femaleCountLiveData.value =  benFlowDao.getOpdCount(genderID, param).value
//    }
//     fun getTotalCount(){
//        _totalCountLiveData.value =  _femaleCountLiveData.value!! + _maleCountLiveData.value!!
//    }

}