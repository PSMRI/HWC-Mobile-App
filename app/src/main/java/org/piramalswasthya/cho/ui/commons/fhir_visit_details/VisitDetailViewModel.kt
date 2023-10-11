package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.ProcedureRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class VisitDetailViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository,
    private val userRepo: UserRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val procedureRepo: ProcedureRepo,
    @ApplicationContext private val application: Context
) : ViewModel() {
    private var _subCatVisitList: LiveData<List<SubVisitCategory>>
    val subCatVisitList: LiveData<List<SubVisitCategory>>
        get() = _subCatVisitList

    private var _lastVisitDate: LiveData<String>?
    val lastVisitDate: LiveData<String>?
        get() = _lastVisitDate

    var base64String = ""
    var fileName = ""
    private var _chiefComplaintMaster: LiveData<List<ChiefComplaintMaster>>
    val chiefComplaintMaster: LiveData<List<ChiefComplaintMaster>>
        get() = _chiefComplaintMaster

    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser
     private var isFollowUpChecked :Boolean = false
    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(application.applicationContext)

    private var _boolCall = MutableLiveData(false)
    val boolCall: LiveData<Boolean>
        get() = _boolCall

    init {
        _subCatVisitList = MutableLiveData()
        _chiefComplaintMaster = MutableLiveData()
        getSubCatVisitList()
        getChiefMasterComplaintList()
        _lastVisitDate = MutableLiveData()
    }
     suspend fun getLastDate(patientID:String) {
        try {
            _lastVisitDate = visitReasonsAndCategoriesRepo.getVisitDbByPatientIDAndBenVisitNo(patientID)
        } catch (e: Exception) {
            Timber.d("Error in Last Visit Date() $e")
        }
    }
    fun setIsFollowUp(boolean: Boolean){
        isFollowUpChecked = boolean
    }
    fun getIsFollowUp():Boolean{
        return isFollowUpChecked
    }
    private fun getSubCatVisitList() {
        try {
            _subCatVisitList = maleMasterDataRepository.getAllSubCatVisit()
        } catch (e: Exception) {
            Timber.d("Error in getSubCatVisitList() $e")
        }
    }

    fun getTheProcedure(patientID: String, benVisitNo: Int){
        viewModelScope.launch {
            val procedureList = procedureRepo.getProceduresWithComponent(patientID, benVisitNo)
            val list = procedureList
        }
    }

    private fun getChiefMasterComplaintList() {
        try {
            _chiefComplaintMaster = maleMasterDataRepository.getChiefMasterComplaint()
        } catch (e: Exception) {
            Timber.d("error in getChiefMasterComplaintList() $e")
        }
    }

    fun getLoggedInUserDetails() {
        viewModelScope.launch {
            try {
                _loggedInUser = userRepo.getUserCacheDetails()
                _boolCall.value = true
            } catch (e: Exception) {
                Timber.d("Error in calling getLoggedInUserDetails() $e")
                _boolCall.value = false
            }
        }
    }

    fun saveVisitDetailsInfo(encounter: Encounter, conditions: List<Condition>) {
        viewModelScope.launch {
            try {
//                fhirEngine.create(encounter)
//                conditions.forEach { condition ->
//                    fhirEngine.create(condition)
//                }
            } catch (e: Exception) {
                Timber.d("Error in Saving Visit Details Informations")
            }
        }
    }

    suspend fun getChiefMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getChiefByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }

    fun resetBool() {
        _boolCall.value = false
    }

    fun setBase64Str(fileStr: String, file: String) {
        base64String = fileStr
        fileName = file
    }

}