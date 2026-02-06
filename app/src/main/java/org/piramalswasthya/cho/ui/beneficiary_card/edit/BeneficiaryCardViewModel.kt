package org.piramalswasthya.cho.ui.beneficiary_card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.PatientDao

@HiltViewModel
class BeneficiaryCardViewModel @Inject constructor(
    private val patientDao: PatientDao
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

    fun refreshPatientInfo(patientID: String) {
        viewModelScope.launch {
            try {
                val patient = patientDao.getPatientDisplayListForNurseByPatient(patientID)
                _patientInfo.value = patient
            } catch (e: Exception) {
                // handle error or keep old data
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
