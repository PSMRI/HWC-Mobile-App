package org.piramalswasthya.cho.ui.ophthalmic_screening

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    private val _reasonForVisit = MutableLiveData<String?>()
    val reasonForVisit: LiveData<String?> = _reasonForVisit

    private val _isDiabetic = MutableLiveData<Boolean?>()
    val isDiabetic: LiveData<Boolean?> = _isDiabetic

    private val _isScreeningPerformed = MutableLiveData<Boolean?>()
    val isScreeningPerformed: LiveData<Boolean?> = _isScreeningPerformed

    private val _chartUsed = MutableLiveData<String?>()
    val chartUsed: LiveData<String?> = _chartUsed

    private val _distVARight = MutableLiveData<String?>()
    val distVARight: LiveData<String?> = _distVARight

    private val _distVALeft = MutableLiveData<String?>()
    val distVALeft: LiveData<String?> = _distVALeft

    private val _nearVA = MutableLiveData<String?>()
    val nearVA: LiveData<String?> = _nearVA

    private val _showScreeningModule = MutableLiveData<Boolean>(false)
    val showScreeningModule: LiveData<Boolean> = _showScreeningModule

    private val _showChartSection = MutableLiveData<Boolean>(false)
    val showChartSection: LiveData<Boolean> = _showChartSection

    private val _showDistVASection = MutableLiveData<Boolean>(false)
    val showDistVASection: LiveData<Boolean> = _showDistVASection

    private val _showNearVASection = MutableLiveData<Boolean>(false)
    val showNearVASection: LiveData<Boolean> = _showNearVASection

    private val _showVisualImpairmentAlert = MutableLiveData<Boolean>()
    val showVisualImpairmentAlert: LiveData<Boolean> = _showVisualImpairmentAlert

    private val _canProceed = MutableLiveData<Boolean>(false)
    val canProceed: LiveData<Boolean> = _canProceed

    private val _saveError = MutableLiveData<Boolean>(false)
    val saveError: LiveData<Boolean> = _saveError

    private var currentPatientId: String? = null
    private var currentBenVisitNo: Int? = null

    fun loadOphthalmicVisit(patientID: String, benVisitNo: Int, reasonForVisit: String) {
        currentPatientId = patientID
        currentBenVisitNo = benVisitNo
        _reasonForVisit.value = reasonForVisit
        _showScreeningModule.value = (reasonForVisit == DropdownConst.screening)
        resetFields()

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
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading ophthalmic visit")
            }
        }
    }

    fun resetFields() {
        _isDiabetic.value = null
        _isScreeningPerformed.value = null
        _chartUsed.value = null
        _distVARight.value = null
        _distVALeft.value = null
        _nearVA.value = null
        _ophthalmicVisit.value = null
        updateSectionVisibility()
        validate()
    }

    private fun updateStateFromVisit(visit: OphthalmicVisit) {
        _isDiabetic.value = visit.isDiabetic
        _isScreeningPerformed.value = visit.screeningPerformed
        _chartUsed.value = visit.visualAcuityChartUsed
        _distVARight.value = visit.distVARight
        _distVALeft.value = visit.distVALeft
        _nearVA.value = visit.nearVA
        updateSectionVisibility()
        validate()
    }

    fun setDiabeticStatus(isDiabetic: Boolean) {
        _isDiabetic.value = isDiabetic
        if (isDiabetic) {
            _chartUsed.value = null
            _nearVA.value = null
        } else {
            _isScreeningPerformed.value = null
        }
        _distVARight.value = null
        _distVALeft.value = null
        updateSectionVisibility()
        validate()
    }

    fun setScreeningPerformed(performed: Boolean) {
        _isScreeningPerformed.value = performed
        if (!performed) {
            _distVARight.value = null
            _distVALeft.value = null
        }
        updateSectionVisibility()
        validate()
    }

    fun setChartUsed(chart: String) {
        _chartUsed.value = chart
        _distVARight.value = null
        _distVALeft.value = null
        _nearVA.value = null
        updateSectionVisibility()
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

    fun setNearVA(value: String) {
        _nearVA.value = value
        validate()
    }

    private fun updateSectionVisibility() {
        val diabetic = _isDiabetic.value
        val screening = _isScreeningPerformed.value
        val chart = _chartUsed.value

        _showChartSection.value = (diabetic == false)

        _showDistVASection.value = when {
            diabetic == true && screening == true -> true
            diabetic == false && chart == DropdownConst.CHART_SNELLENS -> true
            else -> false
        }

        _showNearVASection.value = (diabetic == false && chart == DropdownConst.CHART_NEAR_VISION)
    }

    private fun validate() {
        val reason = _reasonForVisit.value
        val diabetic = _isDiabetic.value
        val screening = _isScreeningPerformed.value
        val chart = _chartUsed.value
        val rightVA = _distVARight.value
        val leftVA = _distVALeft.value
        val nearVaValue = _nearVA.value

        var alert = false
        var caseIdByVA = false
        var fieldsValid = false

        if (reason == DropdownConst.REASON_SYMPTOMATIC) {
            fieldsValid = true
        }

        if (reason == DropdownConst.screening) {
            if (diabetic == null) {
                fieldsValid = false
            } else if (diabetic) {

                if (screening == null) {
                    fieldsValid = false
                } else if (screening) {
                    if (rightVA != null && leftVA != null) {
                        fieldsValid = true
                        if (isVisualImpairment(rightVA) || isVisualImpairment(leftVA)) {
                            alert = true
                            caseIdByVA = true
                        }
                    } else {
                        fieldsValid = false
                    }
                } else {
                    fieldsValid = true
                }
            } else {
                if (chart == null) {
                    fieldsValid = false
                } else if (chart == DropdownConst.CHART_SNELLENS) {
                    if (rightVA != null && leftVA != null) {
                        fieldsValid = true
                        if (isVisualImpairment(rightVA) || isVisualImpairment(leftVA)) {
                            alert = true
                            caseIdByVA = true
                        }
                    } else {
                        fieldsValid = false
                    }
                } else if (chart == DropdownConst.CHART_NEAR_VISION) {
                    if (nearVaValue != null) {
                        fieldsValid = true
                        if (isNearVAReduced(nearVaValue)) {
                            caseIdByVA = true
                        }
                    } else {
                        fieldsValid = false
                    }
                }
            }
        }

        _showVisualImpairmentAlert.value = alert
        _canProceed.value = fieldsValid && !caseIdByVA
    }

    private fun isVisualImpairment(va: String): Boolean {
        return DropdownConst.visualImpairmentList.contains(va)
    }

    private fun isNearVAReduced(va: String?): Boolean {
        return va != null && DropdownConst.nearVAReducedList.contains(va)
    }

    fun save(onSuccess: () -> Unit) {
        _saveError.value = false
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
                    visualAcuityChartUsed = _chartUsed.value
                    distVARight = _distVARight.value
                    distVALeft = _distVALeft.value
                    nearVA = _nearVA.value
                    updatedBy = user?.userName ?: "Unknown"
                    updatedDate = Date().time
                    syncState = 0
                }

                ophthalmicRepository.saveOphthalmicVisit(currentVisit)
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error saving ophthalmic visit")
                _saveError.value = true
            }
        }
    }
}

