package org.piramalswasthya.cho.ui.commons.fhir_patient_vitals

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import org.piramalswasthya.cho.ui.register_patient_activity.patient_details.PhoneNumberValidation
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FhirVitalsViewModel @Inject constructor(@ApplicationContext private val application : Context,
                                              savedStateHandle: SavedStateHandle,
//                                             private var apiInterface: ESanjeevaniApiService,
                                              private val userRepo: UserRepo,
                                              private val visitRepo: VisitReasonsAndCategoriesRepo,
                                              private val vitalsRepo: VitalsRepo,
                                              private val patientRepo: PatientRepo,
                                              private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo

) :
    ViewModel() {

    private val _isDataSaved = MutableLiveData<Boolean>(false)
    val isDataSaved: MutableLiveData<Boolean>
        get() = _isDataSaved

    private var _loggedInUser: UserCache? = null

     var boolTemp:Boolean = false

    var tempNull:Boolean = true
    val loggedInUser: UserCache?
        get() = _loggedInUser
    private var _boolCall = MutableLiveData(false)

    val boolCall: LiveData<Boolean>
        get() = _boolCall
    var questionnaireJson: String? = null

    @SuppressLint("StaticFieldLeak")
    val context: Context = application.applicationContext

    val state = savedStateHandle

    val isEntitySaved = MutableLiveData<Boolean>()

    fun getLoggedInUserDetails(){
        viewModelScope.launch {
            try {
                _loggedInUser = userRepo.getUserCacheDetails()
                _boolCall.value = true
            } catch (e: Exception){
                Timber.d("Error in calling getLoggedInUserDetails() $e")
                _boolCall.value = false
            }
        }
    }
    fun resetBool(){
        _boolCall.value = false
    }

    suspend fun savePatientVitalInfoToCache(patientVitalsModel: PatientVitalsModel){
        vitalsRepo.saveVitalsInfoToCache(patientVitalsModel)
    }
    suspend fun saveVisitDbToCatche(visitDB: VisitDB){
        visitRepo.saveVisitDbToCache(visitDB)
    }

    suspend fun saveChiefComplaintDbToCatche(chiefComplaintDB: ChiefComplaintDB){
        visitRepo.saveChiefComplaintDbToCache(chiefComplaintDB)
    }

    suspend fun savePatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
        val existingPatientVisitInfoSync = patientVisitInfoSyncRepo.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID = patientVisitInfoSync.patientID, benVisitNo = patientVisitInfoSync.benVisitNo)
        if(existingPatientVisitInfoSync != null){
            existingPatientVisitInfoSync.nurseDataSynced = patientVisitInfoSync.nurseDataSynced
            existingPatientVisitInfoSync.doctorDataSynced = patientVisitInfoSync.doctorDataSynced
            existingPatientVisitInfoSync.createNewBenFlow = patientVisitInfoSync.createNewBenFlow
            existingPatientVisitInfoSync.nurseFlag = patientVisitInfoSync.nurseFlag
            existingPatientVisitInfoSync.doctorFlag = patientVisitInfoSync.doctorFlag
            existingPatientVisitInfoSync.visitDate = patientVisitInfoSync.visitDate
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(existingPatientVisitInfoSync)
        }
        else{
            patientVisitInfoSyncRepo.insertPatientVisitInfoSync(patientVisitInfoSync)
        }
    }

    fun saveNurseDataToDb(visitDB: VisitDB, chiefComplaints: List<ChiefComplaintDB>,
                          patientVitals: PatientVitalsModel, patientVisitInfoSync: PatientVisitInfoSync){
        viewModelScope.launch {
            try {
                saveVisitDbToCatche(visitDB)
                chiefComplaints.forEach {
                    saveChiefComplaintDbToCatche(it)
                }
                savePatientVitalInfoToCache(patientVitals)
                savePatientVisitInfoSync(patientVisitInfoSync)
                _isDataSaved.value = true
            } catch (e: Exception){
                _isDataSaved.value = false
            }
        }
    }

    suspend fun hasUnSyncedNurseData(patientId : String) : Boolean{
        return patientVisitInfoSyncRepo.hasUnSyncedNurseData(patientId);
    }

    suspend fun getLastVisitInfoSync(patientId : String) : PatientVisitInfoSync?{
        return patientVisitInfoSyncRepo.getLastVisitInfoSync(patientId, "General OPD");
    }

}