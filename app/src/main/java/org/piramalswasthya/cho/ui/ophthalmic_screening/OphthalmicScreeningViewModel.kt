package org.piramalswasthya.cho.ui.ophthalmic_screening

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.model.OphthalmicVisit
import org.piramalswasthya.cho.repositories.OphthalmicRepository
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.DropdownConst
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OphthalmicScreeningViewModel @Inject constructor(
    private val ophthalmicRepository: OphthalmicRepository,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> = _benAgeGender

    private val _ophthalmicVisit = MutableLiveData<OphthalmicVisit?>()
    val ophthalmicVisit: LiveData<OphthalmicVisit?> = _ophthalmicVisit

    private val _isDiabetic = MutableLiveData<Boolean?>()
    val isDiabetic: LiveData<Boolean?> = _isDiabetic

    private val _isScreeningPerformed = MutableLiveData<Boolean?>()
    val isScreeningPerformed: LiveData<Boolean?> = _isScreeningPerformed

    private val _distVARight = MutableLiveData<String?>()
    val distVARight: LiveData<String?> = _distVARight

    private val _distVALeft = MutableLiveData<String?>()
    val distVALeft: LiveData<String?> = _distVALeft

    private val _showVisualImpairmentAlert = MutableLiveData<Boolean>()
    val showVisualImpairmentAlert: LiveData<Boolean> = _showVisualImpairmentAlert

    private val _enableCaseIdentification = MutableLiveData<Boolean>()
    val enableCaseIdentification: LiveData<Boolean> = _enableCaseIdentification
    
    // Validation State
    private val _canProceed = MutableLiveData<Boolean>(false)
    val canProceed: LiveData<Boolean> = _canProceed

    private var currentPatientId: String? = null
    private var currentBenVisitNo: Int? = null

    fun loadOphthalmicVisit(patientID: String, benVisitNo: Int) {
        currentPatientId = patientID
        currentBenVisitNo = benVisitNo
        viewModelScope.launch {
            try {
                val patientDisplay = patientRepo.getPatientDisplay(patientID)
                _benName.value = "${patientDisplay.patient.firstName ?: ""} ${patientDisplay.patient.lastName ?: ""}".trim()
                val ageText = patientDisplay.patient.age ?: 0
                val ageUnit = patientDisplay.ageUnit?.name ?: ""
                val gender = patientDisplay.gender?.genderName ?: ""
                _benAgeGender.value = "$ageText $ageUnit | $gender"

                val visit = ophthalmicRepository.getOphthalmicVisit(patientID, benVisitNo)
                if (visit != null) {
                    _ophthalmicVisit.value = visit
                    updateStateFromVisit(visit)
                } else {
                    // Initialize default state
                    _isDiabetic.value = null
                    _isScreeningPerformed.value = null
                    validate()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading ophthalmic visit")
                _isDiabetic.value = null
                _isScreeningPerformed.value = null
                validate()
            }
        }
    }

    private fun updateStateFromVisit(visit: OphthalmicVisit) {
        _isDiabetic.value = visit.isDiabetic
        _isScreeningPerformed.value = visit.screeningPerformed
        _distVARight.value = visit.distVARight
        _distVALeft.value = visit.distVALeft
        validate()
    }

    fun setDiabeticStatus(isDiabetic: Boolean) {
        _isDiabetic.value = isDiabetic
        if (!isDiabetic) {
            _isScreeningPerformed.value = null
            _distVARight.value = null
            _distVALeft.value = null
        }
        validate()
    }

    fun setScreeningPerformed(performed: Boolean) {
        _isScreeningPerformed.value = performed
        if (!performed) {
            _distVARight.value = null
            _distVALeft.value = null
        }
        validate()
    }

    fun setDistVARight(value: String) {
        _distVARight.value = value
        validate()
    }

    fun setDistVALeft(value: String) {
        _distVALeft.value = value
        validate()
    }

// ... (existing code)

    private fun validate() {
        val diabetic = _isDiabetic.value
        val screening = _isScreeningPerformed.value
        val rightVA = _distVARight.value
        val leftVA = _distVALeft.value

        var alert = false
        var enableCaseId = false
        var proceed = false

        if (diabetic == true) {
            if (screening == true) {
                if (rightVA != null && leftVA != null) {
                    proceed = true
                    if (isVisualImpairment(rightVA) || isVisualImpairment(leftVA)) {
                        alert = true
                        enableCaseId = true
                    }
                } else {
                    proceed = false
                }
            } else if (screening == false) {
                proceed = true
            } else {
                proceed = false // Screening not selected
            }
        } else if (diabetic == false) {
             proceed = true 
        } else {
            proceed = false // Diabetic not selected
        }

        _showVisualImpairmentAlert.value = alert
        _enableCaseIdentification.value = enableCaseId
        _canProceed.value = proceed
    }

    private fun isVisualImpairment(va: String): Boolean {
        return DropdownConst.visualImpairmentList.contains(va)
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                val patientId = currentPatientId ?: return@launch
                val benVisitNo = currentBenVisitNo ?: return@launch
                
                // Create or update
                val currentVisit = _ophthalmicVisit.value ?: OphthalmicVisit(
                    patientID = patientId,
                    benVisitNo = benVisitNo,
                    createdBy = user?.userName ?: "Unknown",
                    createdDate = Date().time,
                    updatedBy = user?.userName ?: "Unknown",
                    updatedDate = Date().time,
                    syncState = 0
                )

                currentVisit.apply {
                    isDiabetic = _isDiabetic.value
                    screeningPerformed = _isScreeningPerformed.value
                    distVARight = _distVARight.value
                    distVALeft = _distVALeft.value
                    // Update audit fields
                    updatedBy = user?.userName ?: "Unknown"
                    updatedDate = Date().time
                    syncState = 0
                }

                ophthalmicRepository.saveOphthalmicVisit(currentVisit)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving ophthalmic visit")
                // In a real app we might expose an error state
            }
        }
    }
}
