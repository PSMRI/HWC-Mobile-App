package org.piramalswasthya.cho.ui.home_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import java.text.SimpleDateFormat
import java.util.Date

import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val database: InAppDb,
    private var benFlowDao: BenFlowDao,
    private val pref: PreferenceDao,
) : ViewModel() {

}