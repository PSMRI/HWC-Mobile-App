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

    private val _isDiabetic = MutableLiveData<Boolean?>(null)
    val isDiabetic: LiveData<Boolean?> = _isDiabetic

    private val _isScreeningPerformed = MutableLiveData<Boolean?>()
    val isScreeningPerformed: LiveData<Boolean?> = _isScreeningPerformed

    private val _distVARight = MutableLiveData<String?>()
    val distVARight: LiveData<String?> = _distVARight

    private val _distVALeft = MutableLiveData<String?>()
    val distVALeft: LiveData<String?> = _distVALeft

    private val _visualAcuityChartUsed = MutableLiveData<String?>()
    val visualAcuityChartUsed: LiveData<String?> = _visualAcuityChartUsed

    private val _nearVA = MutableLiveData<String?>()
    val nearVA: LiveData<String?> = _nearVA

    private val _showVisualImpairmentAlert = MutableLiveData<Boolean>()
    val showVisualImpairmentAlert: LiveData<Boolean> = _showVisualImpairmentAlert

    private val _showNearVisionAlert = MutableLiveData<Boolean>()
    val showNearVisionAlert: LiveData<Boolean> = _showNearVisionAlert

    private val _enableCaseIdentification = MutableLiveData<Boolean>()
    val enableCaseIdentification: LiveData<Boolean> = _enableCaseIdentification

    // Validation State
    private val _canProceed = MutableLiveData<Boolean>(false)
    val canProceed: LiveData<Boolean> = _canProceed

    // Visibility State
    private val _showDistanceVA = MutableLiveData<Boolean>(false)
    val showDistanceVA: LiveData<Boolean> = _showDistanceVA

    private val _showNearVA = MutableLiveData<Boolean>(false)
    val showNearVA: LiveData<Boolean> = _showNearVA

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
        if (visit.isDiabetic == false && visit.visualAcuityChartUsed == null) {
             _isDiabetic.value = null
        } else {
             _isDiabetic.value = visit.isDiabetic
        }
        _isScreeningPerformed.value = visit.screeningPerformed
        _distVARight.value = visit.distVARight
        _distVALeft.value = visit.distVALeft
        _visualAcuityChartUsed.value = visit.visualAcuityChartUsed
        _nearVA.value = visit.nearVA
        validate()
    }

    fun setDiabeticStatus(isDiabetic: Boolean) {
        _isDiabetic.value = isDiabetic
        if (isDiabetic) {
            _visualAcuityChartUsed.value = null
            _nearVA.value = null
            _distVARight.value = null
            _distVALeft.value = null
        } else {
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

    fun setVisualAcuityChart(value: String) {
        _visualAcuityChartUsed.value = value
        _distVARight.value = null
        _distVALeft.value = null
        _nearVA.value = null
        validate()
    }

    fun setNearVA(value: String) {
        _nearVA.value = value
        validate()
    }

    private fun validate() {
        val diabetic = _isDiabetic.value
        val screening = _isScreeningPerformed.value
        val rightVA = _distVARight.value
        val leftVA = _distVALeft.value
        val chartUsed = _visualAcuityChartUsed.value
        val nearVA = _nearVA.value

        var distanceAlert = false
        var nearAlert = false
        var enableCaseId = false
        var proceed = false

        when {
            diabetic == true -> {
                proceed = validateDiabeticPath(screening, rightVA, leftVA)
                if (proceed && screening == true && rightVA != null && leftVA != null) {
                    distanceAlert = isVisualImpairment(rightVA) || isVisualImpairment(leftVA)
                    enableCaseId = distanceAlert
                }
            }

            diabetic == false -> {
                val result = validateNonDiabeticPath(chartUsed, rightVA, leftVA, nearVA)
                proceed = result.canProceed
                distanceAlert = result.distanceAlert
                nearAlert = result.nearAlert
                enableCaseId = result.enableCaseId
            }
        }

        _showVisualImpairmentAlert.value = distanceAlert
        _showNearVisionAlert.value = nearAlert
        _enableCaseIdentification.value = enableCaseId
        _canProceed.value = proceed

        _showDistanceVA.value = (diabetic == true && _isScreeningPerformed.value == true)
                || (diabetic == false && _visualAcuityChartUsed.value == DropdownConst.SNELLENS_CHART)

        _showNearVA.value = diabetic == false && _visualAcuityChartUsed.value == DropdownConst.NEAR_VISION_CHART
    }

    private fun validateDiabeticPath(
        screening: Boolean?,
        rightVA: String?,
        leftVA: String?
    ): Boolean {
        return when {
            screening == true -> rightVA != null && leftVA != null
            screening == false -> true
            else -> false
        }
    }

    private data class NonDiabeticValidationResult(
        val canProceed: Boolean,
        val distanceAlert: Boolean,
        val nearAlert: Boolean,
        val enableCaseId: Boolean
    )

    private fun validateNonDiabeticPath(
        chartUsed: String?,
        rightVA: String?,
        leftVA: String?,
        nearVA: String?
    ): NonDiabeticValidationResult {
        if (chartUsed == null) {
            return NonDiabeticValidationResult(false, false, false, false)
        }
        return when (chartUsed) {
            DropdownConst.SNELLENS_CHART -> {
                if (rightVA != null && leftVA != null) {
                    val hasImpairment = isVisualImpairment(rightVA) || isVisualImpairment(leftVA)
                    NonDiabeticValidationResult(true, hasImpairment, false, hasImpairment)
                } else {
                    NonDiabeticValidationResult(false, false, false, false)
                }
            }
            DropdownConst.NEAR_VISION_CHART -> {
                if (nearVA != null) {
                    val isReduced = isReducedNearVision(nearVA)
                    NonDiabeticValidationResult(true, false, isReduced, isReduced)
                } else {
                    NonDiabeticValidationResult(false, false, false, false)
                }
            }
            else -> NonDiabeticValidationResult(false, false, false, false)
        }
    }

    private fun isVisualImpairment(va: String): Boolean {
        return DropdownConst.visualImpairmentList.contains(va)
    }

    private fun isReducedNearVision(va: String): Boolean {
        return DropdownConst.reducedNearVisionList.contains(va)
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                val patientId = currentPatientId ?: run {
                    Timber.e("Cannot save: patientId is null")
                    return@launch
                }
                val benVisitNo = currentBenVisitNo ?: run {
                    Timber.e("Cannot save: benVisitNo is null")
                    return@launch
                }
                
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
                    visualAcuityChartUsed = _visualAcuityChartUsed.value
                    nearVA = _nearVA.value
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
