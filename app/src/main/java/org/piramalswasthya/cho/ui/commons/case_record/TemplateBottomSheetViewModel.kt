package org.piramalswasthya.cho.ui.commons.case_record


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import org.piramalswasthya.cho.repositories.PrescriptionTemplateRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TemplateBottomSheetViewModel @Inject constructor(
    private val templateRepo: PrescriptionTemplateRepo
): ViewModel() {
    fun callDel(){
        viewModelScope.launch {
            templateRepo.callDeleteTemplateFromServer()
        }
    }
    fun callMarkDel(string: String){
        try {
            viewModelScope.launch {
                templateRepo.markTemplateDelete(string)
            }
        }catch (e:Exception){
            Timber.tag("XX").i("")
        }
    }
}
