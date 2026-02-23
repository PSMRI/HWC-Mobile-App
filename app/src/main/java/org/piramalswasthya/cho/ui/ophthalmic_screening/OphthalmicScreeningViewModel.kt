package org.piramalswasthya.cho.ui.ophthalmic_screening

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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

    private val moshiAdapter: JsonAdapter<List<String>> by lazy {
        Moshi.Builder().build().adapter(
            Types.newParameterizedType(List::class.java, String::class.java)
        )
    }

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

    private val _caseIdConditions = MutableLiveData<List<String>>(emptyList())
    val caseIdConditions: LiveData<List<String>> = _caseIdConditions

    private val _showScreeningModule = MutableLiveData<Boolean>(false)
    val showScreeningModule: LiveData<Boolean> = _showScreeningModule


    private val _showCaseIdSection = MutableLiveData<Boolean>(false)
    val showCaseIdSection: LiveData<Boolean> = _showCaseIdSection

    private val _isCaseIdMandatory = MutableLiveData<Boolean>(false)
    val isCaseIdMandatory: LiveData<Boolean> = _isCaseIdMandatory

    private val _showChartSection = MutableLiveData<Boolean>(false)
    val showChartSection: LiveData<Boolean> = _showChartSection

    private val _showDistVASection = MutableLiveData<Boolean>(false)
    val showDistVASection: LiveData<Boolean> = _showDistVASection

    private val _showNearVASection = MutableLiveData<Boolean>(false)
    val showNearVASection: LiveData<Boolean> = _showNearVASection

    private val _showVisualImpairmentAlert = MutableLiveData<Boolean>(false)
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
        _showCaseIdSection.value = (reasonForVisit == DropdownConst.REASON_SYMPTOMATIC)
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
        _caseIdConditions.value = emptyList()
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
        

        visit.caseIdConditions?.let { json ->
            try {
                _caseIdConditions.value = moshiAdapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing caseIdConditions")
                _caseIdConditions.value = emptyList()
            }
        } ?: run {
            _caseIdConditions.value = emptyList()
        }

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


    fun setCaseIdConditions(conditions: List<String>) {
        _caseIdConditions.value = conditions

        updateSectionVisibility()
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

    private data class ValidationResult(
        val fieldsValid: Boolean,
        val alert: Boolean,
        val caseIdByVA: Boolean
    )

    private fun validate() {
        val reason = _reasonForVisit.value
        val diabetic = _isDiabetic.value
        val rightVA = _distVARight.value
        val leftVA = _distVALeft.value
        val nearVaValue = _nearVA.value
        val caseIds = _caseIdConditions.value ?: emptyList()

        val isMando = reason == DropdownConst.REASON_SYMPTOMATIC && (
                isVisualImpairment(rightVA ?: "") ||
                isVisualImpairment(leftVA ?: "") ||
                isNearVAReduced(nearVaValue)
        )
        _isCaseIdMandatory.value = isMando

        val result = when {
            reason == DropdownConst.REASON_SYMPTOMATIC -> {
                val validCaseId = if (isMando) caseIds.isNotEmpty() else true
                ValidationResult(fieldsValid = validCaseId, alert = false, caseIdByVA = false)
            }
            reason == DropdownConst.screening && diabetic == true ->
                validateDiabeticPath(rightVA, leftVA)
            reason == DropdownConst.screening && diabetic == false ->
                validateNonDiabeticPath(rightVA, leftVA, nearVaValue)
            else ->
                ValidationResult(fieldsValid = false, alert = false, caseIdByVA = false)
        }

        _showVisualImpairmentAlert.value = result.alert
        // If screening triggers caseIdByVA flag but reason is not symptomatic, it shouldn't ideally block progress if this isn't handled correctly, keeping existing behavior.
        _canProceed.value = result.fieldsValid && !result.caseIdByVA
    }

    private fun validateDiabeticPath(rightVA: String?, leftVA: String?): ValidationResult {
        val screening = _isScreeningPerformed.value
        return when {
            screening == null -> ValidationResult(fieldsValid = false, alert = false, caseIdByVA = false)
            screening -> validateSnellenVA(rightVA, leftVA)
            else -> ValidationResult(fieldsValid = true, alert = false, caseIdByVA = false)
        }
    }

    private fun validateNonDiabeticPath(rightVA: String?, leftVA: String?, nearVaValue: String?): ValidationResult {
        val chart = _chartUsed.value
        return when {
            chart == null -> ValidationResult(fieldsValid = false, alert = false, caseIdByVA = false)
            chart == DropdownConst.CHART_SNELLENS -> validateSnellenVA(rightVA, leftVA)
            chart == DropdownConst.CHART_NEAR_VISION -> validateNearVA(nearVaValue)
            else -> ValidationResult(fieldsValid = false, alert = false, caseIdByVA = false)
        }
    }

    private fun validateSnellenVA(rightVA: String?, leftVA: String?): ValidationResult {
        if (rightVA == null || leftVA == null) {
            return ValidationResult(fieldsValid = false, alert = false, caseIdByVA = false)
        }
        val impaired = isVisualImpairment(rightVA) || isVisualImpairment(leftVA)
        return ValidationResult(fieldsValid = true, alert = impaired, caseIdByVA = impaired)
    }

    private fun validateNearVA(nearVaValue: String?): ValidationResult {
        if (nearVaValue == null) {
            return ValidationResult(fieldsValid = false, alert = false, caseIdByVA = false)
        }
        val reduced = isNearVAReduced(nearVaValue)
        return ValidationResult(fieldsValid = true, alert = false, caseIdByVA = reduced)
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
                
                var conditionsJson: String? = null
                val conditions = _caseIdConditions.value
                if (conditions?.isNotEmpty() == true) {
                    try {
                        conditionsJson = moshiAdapter.toJson(conditions)
                    } catch (e: Exception) {
                        Timber.e(e, "Error converting conditions to JSON")
                    }
                }

                currentVisit.apply {
                    isDiabetic = _isDiabetic.value
                    screeningPerformed = _isScreeningPerformed.value
                    visualAcuityChartUsed = _chartUsed.value
                    distVARight = _distVARight.value
                    distVALeft = _distVALeft.value
                    nearVA = _nearVA.value
                    caseIdConditions = conditionsJson
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