package org.piramalswasthya.cho.ui.commons

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import javax.inject.Inject

@HiltViewModel
class OtherCPHCServicesViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vitalsRepo: VitalsRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,

    ) : ViewModel() {

    private val _isDataSaved = MutableLiveData<Boolean>(false)

    val isDataSaved: MutableLiveData<Boolean>
        get() = _isDataSaved

    suspend fun getLastVisitInfoSync(patientId: String): PatientVisitInfoSync? {
        return patientVisitInfoSyncRepo.getLastVisitInfoSync(patientId);
    }

    fun saveNurseDataToDb(visitDB: VisitDB, patientVitals: PatientVitalsModel, patientVisitInfoSync: PatientVisitInfoSync
    ){
        viewModelScope.launch {
            try {
                saveVisitDbToCatche(visitDB)
                savePatientVitalInfoToCache(patientVitals)
                savePatientVisitInfoSync(patientVisitInfoSync)
                _isDataSaved.value = true
            } catch (e: Exception){
                _isDataSaved.value = false
            }
        }
    }

    suspend fun saveVisitDbToCatche(visitDB: VisitDB){
        visitReasonsAndCategoriesRepo.saveVisitDbToCache(visitDB)
    }

    suspend fun savePatientVitalInfoToCache(patientVitalsModel: PatientVitalsModel){
        vitalsRepo.saveVitalsInfoToCache(patientVitalsModel)
    }

    suspend fun savePatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
        val existingPatientVisitInfoSync = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID = patientVisitInfoSync.patientID, benVisitNo = patientVisitInfoSync.benVisitNo)
        if(existingPatientVisitInfoSync != null){
            existingPatientVisitInfoSync.nurseDataSynced = patientVisitInfoSync.nurseDataSynced
            existingPatientVisitInfoSync.doctorDataSynced = patientVisitInfoSync.doctorDataSynced
            existingPatientVisitInfoSync.createNewBenFlow = patientVisitInfoSync.createNewBenFlow
            existingPatientVisitInfoSync.nurseFlag = patientVisitInfoSync.nurseFlag
            existingPatientVisitInfoSync.doctorFlag = patientVisitInfoSync.doctorFlag
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(existingPatientVisitInfoSync)
        }
        else{
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
        }
    }

}