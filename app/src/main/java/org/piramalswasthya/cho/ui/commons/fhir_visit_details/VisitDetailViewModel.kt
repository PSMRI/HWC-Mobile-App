package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.Context
import android.util.Log
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
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class VisitDetailViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository,
    private val userRepo: UserRepo,
    @ApplicationContext private val application : Context
): ViewModel(){
    private  var _subCatVisitList: LiveData<List<SubVisitCategory>>
    val subCatVisitList:LiveData<List<SubVisitCategory>>
        get() = _subCatVisitList


    private  var _chiefComplaintMaster: LiveData<List<ChiefComplaintMaster>>
    val chiefComplaintMaster:LiveData<List<ChiefComplaintMaster>>
        get() = _chiefComplaintMaster

    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser

    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(application.applicationContext)
    init{
        _subCatVisitList = MutableLiveData()
        _chiefComplaintMaster = MutableLiveData()
        getSubCatVisitList()
        getChiefMasterComplaintList()
    }
    private fun getSubCatVisitList(){
        try{
               _subCatVisitList  = maleMasterDataRepository.getAllSubCatVisit()
                Log.i("Okay bva;","Here is the list $_subCatVisitList")

        } catch (e: Exception){
            Timber.d("Error in getSubCatVisitList() $e")
        }
    }

    private fun getChiefMasterComplaintList(){
        try {
            _chiefComplaintMaster = maleMasterDataRepository.getChiefMasterComplaint()
        }catch (e: Exception){
            Timber.d("error in getChiefMasterComplaintList() $e")
        }
    }
    fun getLoggedInUserDetails(){
        viewModelScope.launch {
           try {
               _loggedInUser = userRepo.getUserCacheDetails()
           } catch (e: Exception){
               Timber.d("Error in calling getLoggedInUserDetails() $e")
           }
        }
    }

    fun saveVisitDetailsInfo(encounter: Encounter,conditions: List<Condition>){
        viewModelScope.launch {
            try{
                fhirEngine.create(encounter)
                conditions.forEach { condition ->
                    fhirEngine.create(condition)
                }
            } catch (e: Exception){
                Timber.d("Error in Saving Visit Details Informations")
            }
        }
    }

}