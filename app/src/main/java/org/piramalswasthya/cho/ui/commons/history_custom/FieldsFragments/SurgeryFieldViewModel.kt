package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject
@HiltViewModel
class SurgeryFieldViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository
): ViewModel() {

    private var _surgeryDropdown: LiveData<List<SurgeryDropdown>>

    val surgeryDropdown: LiveData<List<SurgeryDropdown>>
        get() = _surgeryDropdown

    init{
        _surgeryDropdown = MutableLiveData()
        getSurgeryDropdown()
    }
    private fun getSurgeryDropdown(){
        try{
            _surgeryDropdown  = maleMasterDataRepository.getAllSurgeryDropdown()

        } catch (e: Exception){
            Timber.d("Error in getSurgeryList() $e")
        }
    }

}