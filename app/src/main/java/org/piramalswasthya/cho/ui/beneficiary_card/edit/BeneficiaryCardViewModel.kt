package org.piramalswasthya.cho.ui.beneficiary_card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import javax.inject.Inject

@HiltViewModel
class BeneficiaryCardViewModel @Inject constructor() : ViewModel() {

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
