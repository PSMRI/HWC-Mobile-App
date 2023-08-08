package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.TobaccoDropdown
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject
@HiltViewModel
class TobaccoFieldViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository
): ViewModel() {

    private var _tobaccoDropdown: LiveData<List<TobaccoDropdown>>

    val tobaccoDropdown: LiveData<List<TobaccoDropdown>>
        get() = _tobaccoDropdown

    init{
        _tobaccoDropdown = MutableLiveData()
        getTobaccoDropdown()
    }
    private fun getTobaccoDropdown(){
        try{
            _tobaccoDropdown  = maleMasterDataRepository.getAllTobaccoDropdown()

        } catch (e: Exception){
            Timber.d("Error in getTobaccoList() $e")
        }
    }

}