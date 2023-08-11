package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class IllnessFieldViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository
): ViewModel() {

    private var _illnessDropdown: LiveData<List<IllnessDropdown>>

    val illnessDropdown: LiveData<List<IllnessDropdown>>
        get() = _illnessDropdown

    init{
        _illnessDropdown = MutableLiveData()
        getIllnessDropdown()
    }
    private fun getIllnessDropdown(){
        try{
            _illnessDropdown  = maleMasterDataRepository.getAllIllnessDropdown()

        } catch (e: Exception){
            Timber.d("Error in getIllergyList() $e")
        }
    }

}