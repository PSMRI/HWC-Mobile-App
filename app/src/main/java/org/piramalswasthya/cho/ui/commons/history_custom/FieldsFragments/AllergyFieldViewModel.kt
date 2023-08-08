package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject
@HiltViewModel
class AllergyFieldViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository
): ViewModel() {

    private var _allergyDropdown: LiveData<List<AllergicReactionDropdown>>

    val allergyDropdown: LiveData<List<AllergicReactionDropdown>>
        get() = _allergyDropdown

    init {
        _allergyDropdown = MutableLiveData()
        getAllergyDropdown()
    }

    private fun getAllergyDropdown() {
        try {
            _allergyDropdown = maleMasterDataRepository.getAllAllergyDropdown()

        } catch (e: Exception) {
            Timber.d("Error in getAllergyList() $e")
        }
    }
}