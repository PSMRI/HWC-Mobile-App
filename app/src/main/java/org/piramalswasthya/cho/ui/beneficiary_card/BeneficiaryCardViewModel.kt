package org.piramalswasthya.cho.ui.beneficiary_card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.repositories.PatientRepo
import javax.inject.Inject

@HiltViewModel
class BeneficiaryCardViewModel @Inject constructor(
    private val patientRepo: PatientRepo
) : ViewModel() {

    private val _patientInfo = MutableLiveData<PatientDisplayWithVisitInfo?>()
    val patientInfo: LiveData<PatientDisplayWithVisitInfo?>
        get() = _patientInfo

    private val _navigateToAbha = MutableLiveData<Boolean>()
    val navigateToAbha: LiveData<Boolean>
        get() = _navigateToAbha

    private val _navigateToContinue = MutableLiveData<Boolean>()
    val navigateToContinue: LiveData<Boolean>
        get() = _navigateToContinue

    fun setPatientInfo(patient: PatientDisplayWithVisitInfo) {
        _patientInfo.value = patient
    }

    fun reloadPatientData(patientID: String) {
        viewModelScope.launch {
            try {
                val refreshedPatient = patientRepo.getPatientDisplayListForNurseByPatient(patientID)
                _patientInfo.value = refreshedPatient
            } catch (e: Exception) {
                // If reload fails, keep existing data
                // Error handling can be added here if needed
            }
        }
    }

    fun onGenerateAbhaClicked() {
        _navigateToAbha.value = true
    }

    fun onAbhaNavigated() {
        _navigateToAbha.value = false
    }

    fun onContinueClicked() {
        _navigateToContinue.value = true
    }

    fun onContinueNavigated() {
        _navigateToContinue.value = false
    }
}
