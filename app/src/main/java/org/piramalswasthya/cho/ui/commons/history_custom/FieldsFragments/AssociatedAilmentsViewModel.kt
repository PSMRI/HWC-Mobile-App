package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.model.FamilyMemberDropdown
import org.piramalswasthya.cho.model.AssociateAilmentsDropdown
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AssociatedAilmentsViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository
): ViewModel() {

    private var _associateAilmentsDropdown: LiveData<List<AssociateAilmentsDropdown>>

    val associateAilmentsDropdown: LiveData<List<AssociateAilmentsDropdown>>
        get() = _associateAilmentsDropdown

    private var _familyDropdown: LiveData<List<FamilyMemberDropdown>>

    val familyDropdown: LiveData<List<FamilyMemberDropdown>>
        get() = _familyDropdown

    init{
        _familyDropdown = MutableLiveData()
        getFamilyDropdown()

        _associateAilmentsDropdown = MutableLiveData()
        getAssociateAilmentsDropdown()
    }
    private fun getAssociateAilmentsDropdown(){
        try{
            _associateAilmentsDropdown  = maleMasterDataRepository.getAssociateAilments()
        } catch (e: Exception){
            Timber.d("Error in getAssociateAilmentsDropdown() $e")
        }
    }
    private fun getFamilyDropdown(){
        try{
            _familyDropdown  = maleMasterDataRepository.getAllFamilyMemberDropdown()

        } catch (e: Exception){
            Timber.d("Error in getFamilyList() $e")
        }
    }

}