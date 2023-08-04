package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.ChiefComplaintMasterDao
import org.piramalswasthya.cho.database.room.dao.SubCatVisitDao
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class VisitDetailViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository
    ): ViewModel() {
    private  var _subCatVisitList: LiveData<List<SubVisitCategory>>
    val subCatVisitList:LiveData<List<SubVisitCategory>>
        get() = _subCatVisitList


    private  var _chiefComplaintMaster: LiveData<List<ChiefComplaintMaster>>
    val chiefComplaintMaster:LiveData<List<ChiefComplaintMaster>>
        get() = _chiefComplaintMaster


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
}