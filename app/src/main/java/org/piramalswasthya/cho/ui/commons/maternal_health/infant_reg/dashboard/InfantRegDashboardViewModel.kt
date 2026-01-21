package org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import javax.inject.Inject

@HiltViewModel
class InfantRegDashboardViewModel @Inject constructor(
    private val patientRepo: PatientRepo
) : ViewModel() {
    
    private val allPatientList = patientRepo.getPNCActiveWomenWithBabiesList()
    private val filter = MutableStateFlow("")
    
    val patientList = allPatientList.combine(filter) { list, filterText ->
        if (filterText.isEmpty()) {
            list
        } else {
            list.filter { patient ->
                val fullName = "${patient.patient.firstName ?: ""} ${patient.patient.lastName ?: ""}".trim()
                fullName.contains(filterText, ignoreCase = true) ||
                patient.patient.patientID.contains(filterText, ignoreCase = true)
            }
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }
}
