package org.piramalswasthya.cho.ui.home_activity

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDoctorBundle
import org.piramalswasthya.cho.model.PatientVisitDataBundle
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionWithItemMasterAndDrugFormMaster
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PrescriptionTemplateRepo
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.ui.home.DataLoadFlagManager
import org.piramalswasthya.cho.work.WorkerUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeActivityViewModel @Inject constructor (application: Application,
                                                 private val database: InAppDb,
                                                 private val pref: PreferenceDao,
                                                 private val userRepo: UserRepo,
                                                 private val userDao: UserDao,
                                                 private val benFlowRepo: BenFlowRepo,
                                                 private val patientRepo: PatientRepo,
                                                 private val registrarMasterDataRepo: RegistrarMasterDataRepo,
                                                 private val languageRepo: LanguageRepo,
                                                 private val vitalsDao: VitalsDao,
                                                 private val patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
                                                 private val visitReasonsAndCategoriesDao: VisitReasonsAndCategoriesDao,
                                                 private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
                                                 private val vaccineAndDoseTypeRepo: VaccineAndDoseTypeRepo,
                                                 private val malMasterDataRepo: MaleMasterDataRepository,
                                                 private val doctorMaleMasterDataRepo: DoctorMasterDataMaleRepo,
                                                 private val prescriptionTemplateRepo: PrescriptionTemplateRepo,
                                                 private val dataLoadFlagManager: DataLoadFlagManager) : AndroidViewModel(application) {


    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    fun init(context: Context){
        viewModelScope.launch {
            extracted(context)
//            getStockDetailsOfSubStore()
        }
    }

    fun triggerDownSyncWorker(context: Context, syncName: String){
        if (dataLoadFlagManager.isDataLoaded()){
            Log.d("triggering down", "down trigger")
            WorkerUtils.triggerDownSyncWorker(context, syncName)
        }
    }

    private suspend fun extracted(context: Context) {
        try {
            _state.postValue(State.SAVING)
            if (dataLoadFlagManager.isDataLoaded()){
                Log.d("syncing started first", "syncing started")
                WorkerUtils.triggerAmritSyncWorker(context)
            }
            WorkerUtils.pushAuditDetailsWorker(context)
            registrarMasterDataRepo.saveGenderMasterResponseToCache()
            registrarMasterDataRepo.saveAgeUnitMasterResponseToCache()
            registrarMasterDataRepo.saveMaritalStatusServiceResponseToCache()
            registrarMasterDataRepo.saveCommunityMasterResponseToCache()
            registrarMasterDataRepo.saveReligionMasterResponseToCache()
            languageRepo.saveResponseToCacheLang()
            visitReasonsAndCategoriesRepo.saveVisitReasonResponseToCache()
            visitReasonsAndCategoriesRepo.saveVisitCategoriesResponseToCache()
            registrarMasterDataRepo.saveIncomeMasterResponseToCache()
            registrarMasterDataRepo.saveLiteracyStatusServiceResponseToCache()
            registrarMasterDataRepo.saveGovIdEntityMasterResponseToCache()
            registrarMasterDataRepo.saveOtherGovIdEntityMasterResponseToCache()
            registrarMasterDataRepo.saveOccupationMasterResponseToCache()
            registrarMasterDataRepo.saveQualificationMasterResponseToCache()
            registrarMasterDataRepo.saveRelationshipMasterResponseToCache()
            vaccineAndDoseTypeRepo.saveVaccineTypeResponseToCache()

            prescriptionTemplateRepo.getTemplateFromServer(userRepo.getLoggedInUser()!!.userId)
            vaccineAndDoseTypeRepo.saveDoseTypeResponseToCache()
            vaccineAndDoseTypeRepo.getVaccineDetailsFromServer()
            doctorMaleMasterDataRepo.getDoctorMasterMaleData()

            malMasterDataRepo.getMasterDataForNurse()
            getStockDetailsOfSubStore()
            if (!dataLoadFlagManager.isDataLoaded()){
                Log.d("syncing started second", "syncing started")
                WorkerUtils.triggerAmritSyncWorker(context)
            }
            dataLoadFlagManager.setDataLoaded(true)
            _state.postValue(State.SAVE_SUCCESS)
        } catch (_e: Exception) {
            Log.d("Exception coming is", _e.toString())
            _state.postValue(State.SAVE_FAILED)
        }
    }

    private val _navigateToLoginPage = MutableLiveData(false)
    val navigateToLoginPage: MutableLiveData<Boolean>
        get() = _navigateToLoginPage

    fun logout(myLocation:Location?,logoutType: String) {
        viewModelScope.launch {
            val user = userDao.getLoggedInUser()
            val lat = myLocation?.latitude
            val long = myLocation?.longitude
            val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
            val timeZone = TimeZone.getTimeZone("GMT+0530")
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.timeZone = timeZone

            val logoutTimestamp = formatter.format(Date())

            val selectedOutreachProgram = SelectedOutreachProgram(0,
                user?.userId,
                user?.userName,
                null,
                null,
                logoutTimestamp,
                null,
                lat,
                long,
                logoutType,
            null)
            userDao.insertOutreachProgram(selectedOutreachProgram)
            userDao.resetAllUsersLoggedInState()
            if (user != null) {
                userDao.updateLogoutTime(user.userId,Date())
            }
            pref.deleteEsanjeevaniCreds()
            _navigateToLoginPage.value = true
        }
    }

    fun insertPatient(patient: Patient){
        viewModelScope.launch {
            val existingPatient = patientRepo.getPatient(patientId = patient.patientID)
            if(existingPatient == null){
                patient.syncState = SyncState.SHARED_OFFLINE
                 patientRepo.insertPatient(patient)
            }
        }
    }

    suspend fun insertPatient1(patient: Patient) {
        val existingPatient = patientRepo.getPatient(patientId = patient.patientID)
        if (existingPatient == null) {
            patientRepo.insertPatient(patient)
        }
    }

    suspend fun checkAndAddNewVisitInfoOffline(patientVisitInfoSync: PatientVisitInfoSync) {
        val existingPatientVisitInfoSync = patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
            patientVisitInfoSync.patientID, patientVisitInfoSync.benVisitNo!!
        )
        if (existingPatientVisitInfoSync == null) {
            patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
        }
    }

    suspend fun checkAndOfflineSyncNurseData(
        visit: VisitDB,
        chiefComplaints: List<ChiefComplaintDB>,
        vitals: PatientVitalsModel,
        patientID: String,
        benVisitNo: Int
    ) {
        withContext(Dispatchers.IO) {
            visitReasonsAndCategoriesDao.deleteVisitDbByPatientIdAndBenVisitNo(patientID, benVisitNo)
            visitReasonsAndCategoriesDao.insertVisitDB(visit)
            visitReasonsAndCategoriesDao.deleteChiefComplaintsByPatientIdAndBenVisitNo(patientID, benVisitNo)
            chiefComplaints?.let {
                visitReasonsAndCategoriesDao.insertAll(chiefComplaints)
            }
            vitalsDao.insertPatientVitals(vitals)
        }
    }

    fun processPatientVisitDataBundle(patientVisitDataBundle: PatientVisitDataBundle) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Insert patient
                patientVisitDataBundle.patient.syncState = SyncState.UNSYNCED
                insertPatient1(patientVisitDataBundle.patient)

                // Add new visit info
                patientVisitDataBundle.patientVisitInfoSync.nurseDataSynced = SyncState.UNSYNCED
                checkAndAddNewVisitInfoOffline(patientVisitDataBundle.patientVisitInfoSync)

                // Sync nurse data
                checkAndOfflineSyncNurseData(
                    patientVisitDataBundle.visit,
                    patientVisitDataBundle.chiefComplaints,
                    patientVisitDataBundle.vitals,
                    patientVisitDataBundle.patient.patientID,
                    patientVisitDataBundle.patientVisitInfoSync.benVisitNo
                )
            }
        }
    }

    fun processPatientDoctorBundle(dummyPatientDoctorBundle: PatientDoctorBundle){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                // insertPatient1(patientDoctorBundle.patient)
                try {
                   // patientRepo.insertPatientIfNoDuplicate(dummyPatientDoctorBundle.patient)
                    dummyPatientDoctorBundle.patient.syncState = SyncState.SHARED_OFFLINE
                    patientRepo.insertPatient(dummyPatientDoctorBundle.patient)
                    Log.d("Pharmacist", " patient inserted ")
                }catch (e:Exception){
                    e.printStackTrace()
                    Log.d("Pharmacist",e.message.toString())
                }
                try {
                    dummyPatientDoctorBundle.patientVisitInfoSync.pharmacist_flag = 1;
                   // checkAndAddNewVisitInfoOffline(dummyPatientDoctorBundle.patientVisitInfoSync)
                    val existingPatientVisitInfoSync = patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
                        dummyPatientDoctorBundle.patientVisitInfoSync.patientID,  dummyPatientDoctorBundle.patientVisitInfoSync.benVisitNo!!
                    )
                    if (existingPatientVisitInfoSync == null) {
                        dummyPatientDoctorBundle.patientVisitInfoSync.nurseDataSynced = SyncState.SYNCED
                        dummyPatientDoctorBundle.patientVisitInfoSync.doctorDataSynced = SyncState.SYNCED
                        dummyPatientDoctorBundle.patientVisitInfoSync.pharmacistDataSynced = SyncState.SYNCED
                        patientVisitInfoSyncDao.insertPatientVisitInfoSync( dummyPatientDoctorBundle.patientVisitInfoSync)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                    Log.d("Pharmacist",e.message.toString())
                }
                val facilityID = userDao.getLoggedInUserFacilityID()
                try {
                    benFlowRepo.savePrescriptionListForPharmacist(
                        dummyPatientDoctorBundle.patient,
                        facilityID,
                        dummyPatientDoctorBundle,
                        dummyPatientDoctorBundle.patientVisitInfoSync
                    )
                }catch (e:Exception){
                    e.printStackTrace()
                    Log.d("Pharmacist",e.message.toString())
                }
            }
        }
    }

    suspend fun getStockDetailsOfSubStore(){
        withContext(Dispatchers.IO) {
            val facilityID = userDao.getLoggedInUserFacilityID()
            benFlowRepo.getStockDetailsOfSubStore(facilityID)
        }
    }

    fun navigateToLoginPageComplete() {
        _navigateToLoginPage.value = false
    }

    companion object {

        private val _state = MutableLiveData(State.IDLE)

        val state: LiveData<State>
            get() = _state
    }

}