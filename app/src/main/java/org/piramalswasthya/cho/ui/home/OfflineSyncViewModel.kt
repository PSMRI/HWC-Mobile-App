package org.piramalswasthya.cho.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDoctorBundle
import org.piramalswasthya.cho.model.PatientVisitDataBundle
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import javax.inject.Inject

@HiltViewModel
class OfflineSyncViewModel @Inject constructor(
    private val patientDao: PatientDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vitalsRepo: VitalsRepo,
    private val caseRecordeRepo: CaseRecordeRepo,
) : ViewModel() {

    var patients = emptyList<PatientDisplay>()
    val patientVisitInfoSyncList = mutableListOf<PatientVisitInfoSync>()
    val visitList = mutableListOf<VisitDB>()
    val chiefComplaintsList = mutableListOf<List<ChiefComplaintDB>>()
    val vitalsList = mutableListOf<PatientVitalsModel>()
    val  patientVisitDataBundle = mutableListOf<PatientVisitDataBundle>()
    val  patientDoctorBundle = mutableListOf<PatientDoctorBundle>()
    var userRole = ""
    private lateinit var user: UserDomain
    var userName = ""

    init {
        getUserDetails()
        //handle case for user which is both reg and nurse -> & !isUserNurse()
//        if(preferenceDao.isUserRegistrar() && preferenceDao.isUserStaffNurseOrNurse()){
//            userRole = "Registrar&Nurse"
//            getUnsyncedRegistrarData()
//            getUnsyncedNurseData()
//        }else

        if (preferenceDao.isRegistrarSelected()) {
            userRole = "Registrar"
            getUnsyncedRegistrarData()
        } else if (preferenceDao.isNurseSelected()) {
            userRole = "Nurse"
            getUnsyncedNurseData()
        }else if(preferenceDao.isUserDoctorOrMO()){
            Log.d("Doctor","True")
            userRole = "Doctor"
            getUnsyncedDoctorData()
        }
    }

    fun getUserDetails() {
        viewModelScope.launch {
            user = userRepo.getLoggedInUser()!!
            userName = user.userName
        }
    }

    fun getUnsyncedRegistrarData() {
        _state.postValue(State.FETCHING)
        try {
            viewModelScope.launch {
                patients = patientDao.getPatientListUnsynced()
                if(userRole == "Registrar") {
                    _state.postValue(State.SUCCESS)
                }
            }
        } catch (e: Exception) {
            Log.e("OfflineSyncViewModel", "Error fetching unsynced registrar data", e)
            _state.postValue(State.FAILED)
        }
    }

    fun getUnsyncedNurseData() {
        _state.postValue(State.FETCHING)
        try {
            viewModelScope.launch {
                val patientNurseDataUnSyncList =
                    patientVisitInfoSyncRepo.getPatientNurseDataUnsynced()
                patientNurseDataUnSyncList.forEach {
                    val patient = patientDao.getPatient(it.patient.patientID)
                    val visit = visitReasonsAndCategoriesRepo.getVisitDbByPatientIDAndBenVisitNo(
                        patientID = it.patient.patientID,
                        benVisitNo = it.patientVisitInfoSync.benVisitNo
                    )
                    val chiefComplaints =
                        visitReasonsAndCategoriesRepo.getChiefComplaintsByPatientIDAndBenVisitNo(
                            patientID = it.patient.patientID,
                            benVisitNo = it.patientVisitInfoSync.benVisitNo
                        )
                    val vitals = vitalsRepo.getPatientVitalsByPatientIDAndBenVisitNo(
                        patientID = it.patient.patientID,
                        benVisitNo = it.patientVisitInfoSync.benVisitNo
                    )
                    patientVisitInfoSyncList.add(it.patientVisitInfoSync)
                    visitList.add(visit!!)
                    chiefComplaintsList.add(chiefComplaints!!)
                    vitalsList.add(vitals!!)
                    patientVisitDataBundle.add(
                        PatientVisitDataBundle(
                            patient = patient,
                            patientVisitInfoSync = it.patientVisitInfoSync,
                            visit = visit,
                            chiefComplaints = chiefComplaints,
                            vitals = vitals
                        )
                    )
                }
                _state.postValue(State.SUCCESS)
            }
        } catch (e: Exception) {
            Log.e("OfflineSyncViewModel", "Error fetching unsynced nurse data", e)
            _state.postValue(State.FAILED)
        }
    }

    fun getUnsyncedDoctorData(){
        Log.d("Doctor","getUnsyncedDoctorData")
        _state.postValue(State.FETCHING)
        try {
            viewModelScope.launch {
                val patientDoctorDataUnSyncList =  patientVisitInfoSyncRepo.getPatientDoctorDataUnsyncedForOfflineTransfer()
                Log.d("Doctor","patientDoctorDataUnSyncList ${patientDoctorDataUnSyncList}")
                patientDoctorDataUnSyncList.forEach{
                    val patient = patientDao.getPatient(it.patient.patientID)
                    Log.d("Pharmacist", "Patient:in offline viewmodel ${patient}")
                    val prescriptionCaseRecordVal = caseRecordeRepo.getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                    patientDoctorBundle.add(
                        PatientDoctorBundle(
                            patient = patient,
                            patientVisitInfoSync = it.patientVisitInfoSync,
                            prescriptionCaseRecordVal = prescriptionCaseRecordVal
                        )
                    )
                }
                _state.postValue(State.SUCCESS)
                Log.d("Discovery", "getUnsyncedDoctorData: ${patientDoctorBundle}")
            }
        }catch (e: Exception){
            _state.postValue(State.FAILED)
            Log.e("OfflineSyncViewModel", "Error fetching unsynced doctor data", e)
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