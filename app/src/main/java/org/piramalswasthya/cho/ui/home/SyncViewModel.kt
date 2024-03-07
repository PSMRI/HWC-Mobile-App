package org.piramalswasthya.cho.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
) : ViewModel(){

    val syncStatus = patientVisitInfoSyncDao.getSyncStatus()

}