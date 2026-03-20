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

    private val _cataractSymptoms = MutableLiveData<Boolean?>()
    val cataractSymptoms: LiveData<Boolean?> = _cataractSymptoms

    private val _glaucomaSymptoms = MutableLiveData<Boolean?>()
    val glaucomaSymptoms: LiveData<Boolean?> = _glaucomaSymptoms

    private val _drSymptoms = MutableLiveData<Boolean?>()
    val drSymptoms: LiveData<Boolean?> = _drSymptoms

    private val _presbyopiaSymptoms = MutableLiveData<Boolean?>()
    val presbyopiaSymptoms: LiveData<Boolean?> = _presbyopiaSymptoms

    private val _trachomaStatus = MutableLiveData<String?>()
    val trachomaStatus: LiveData<String?> = _trachomaStatus

    private val _cornealDiseaseType = MutableLiveData<String?>()
    val cornealDiseaseType: LiveData<String?> = _cornealDiseaseType

    private val _vitaminADeficiency = MutableLiveData<Boolean?>()
    val vitaminADeficiency: LiveData<Boolean?> = _vitaminADeficiency

    private val _showCataractSubField = MutableLiveData<Boolean>(false)
    val showCataractSubField: LiveData<Boolean> = _showCataractSubField

    private val _showGlaucomaSubField = MutableLiveData<Boolean>(false)
    val showGlaucomaSubField: LiveData<Boolean> = _showGlaucomaSubField

    private val _showDrSubField = MutableLiveData<Boolean>(false)
    val showDrSubField: LiveData<Boolean> = _showDrSubField

    private val _showPresbyopiaSubField = MutableLiveData<Boolean>(false)
    val showPresbyopiaSubField: LiveData<Boolean> = _showPresbyopiaSubField

    private val _showTrachomaSubField = MutableLiveData<Boolean>(false)
    val showTrachomaSubField: LiveData<Boolean> = _showTrachomaSubField

    private val _showCornealSubField = MutableLiveData<Boolean>(false)
    val showCornealSubField: LiveData<Boolean> = _showCornealSubField

    private val _showVitaminASubField = MutableLiveData<Boolean>(false)
    val showVitaminASubField: LiveData<Boolean> = _showVitaminASubField

    private val _showCataractAlert = MutableLiveData<Boolean>(false)
    val showCataractAlert: LiveData<Boolean> = _showCataractAlert

    private val _showGlaucomaAlert = MutableLiveData<Boolean>(false)
    val showGlaucomaAlert: LiveData<Boolean> = _showGlaucomaAlert

    private val _showDrAlert = MutableLiveData<Boolean>(false)
    val showDrAlert: LiveData<Boolean> = _showDrAlert

    private val _showPresbyopiaAlert = MutableLiveData<Boolean>(false)
    val showPresbyopiaAlert: LiveData<Boolean> = _showPresbyopiaAlert

    private val _showTrachomaAlert = MutableLiveData<Boolean>(false)
    val showTrachomaAlert: LiveData<Boolean> = _showTrachomaAlert

    private val _showCornealAlert = MutableLiveData<Boolean>(false)
    val showCornealAlert: LiveData<Boolean> = _showCornealAlert

    private val _showVitaminAAlert = MutableLiveData<Boolean>(false)
    val showVitaminAAlert: LiveData<Boolean> = _showVitaminAAlert

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

    private val _showInjuryTraumaModule = MutableLiveData<Boolean>(false)
    val showInjuryTraumaModule: LiveData<Boolean> = _showInjuryTraumaModule

    private val _injuryTypes = MutableLiveData<List<String>>(emptyList())
    val injuryTypes: LiveData<List<String>> = _injuryTypes

    private val _foreignBodyRemoval = MutableLiveData<String?>()
    val foreignBodyRemoval: LiveData<String?> = _foreignBodyRemoval

    private val _chemicalExposure = MutableLiveData<Boolean?>()
    val chemicalExposure: LiveData<Boolean?> = _chemicalExposure

    private val _showForeignBodyRemovalField = MutableLiveData<Boolean>(false)
    val showForeignBodyRemovalField: LiveData<Boolean> = _showForeignBodyRemovalField

    private val _showChemicalExposureField = MutableLiveData<Boolean>(false)
    val showChemicalExposureField: LiveData<Boolean> = _showChemicalExposureField

    private val _showForeignBodyAlert = MutableLiveData<Boolean>(false)
    val showForeignBodyAlert: LiveData<Boolean> = _showForeignBodyAlert

    private val _showChemicalExposureAlert = MutableLiveData<Boolean>(false)
    val showChemicalExposureAlert: LiveData<Boolean> = _showChemicalExposureAlert

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
        _showInjuryTraumaModule.value = isInjuryTraumaReason(reasonForVisit)
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
        _injuryTypes.value = emptyList()
        _foreignBodyRemoval.value = null
        _chemicalExposure.value = null
        _showForeignBodyRemovalField.value = false
        _showChemicalExposureField.value = false
        _showForeignBodyAlert.value = false
        _showChemicalExposureAlert.value = false
        _ophthalmicVisit.value = null
        resetConditionSubFields()
        updateSectionVisibility()
        validate()
    }

    private fun resetConditionSubFields() {
        _cataractSymptoms.value = null
        _glaucomaSymptoms.value = null
        _drSymptoms.value = null
        _presbyopiaSymptoms.value = null
        _trachomaStatus.value = null
        _cornealDiseaseType.value = null
        _vitaminADeficiency.value = null
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

        _cataractSymptoms.value = visit.cataractSymptoms
        _glaucomaSymptoms.value = visit.glaucomaSymptoms
        _drSymptoms.value = visit.diabeticRetinopathySymptoms
        _presbyopiaSymptoms.value = visit.presbyopiaSymptoms
        _trachomaStatus.value = visit.trachomaStatus
        _cornealDiseaseType.value = visit.cornealDiseaseType
        _vitaminADeficiency.value = visit.vitaminADeficiency
        _foreignBodyRemoval.value = visit.foreignBodyRemoval
        _chemicalExposure.value = visit.chemicalExposure

        visit.injuryType?.let { json ->
            try {
                _injuryTypes.value = moshiAdapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing injuryType")
                _injuryTypes.value = emptyList()
            }
        } ?: run {
            _injuryTypes.value = emptyList()
        }

        updateSectionVisibility()
        deriveAlerts()
        deriveInjuryAlerts()
        validate()
    }

    // ─── Public setters ─────────────────────────────────────────────────────

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
        clearRemovedConditionSubFields(conditions)
        updateConditionSubFieldVisibility(conditions)
        deriveAlerts()
        validate()
    }

    fun setCataractSymptoms(hasSymptoms: Boolean) {
        _cataractSymptoms.value = hasSymptoms
        deriveAlerts()
        validate()
    }

    fun setGlaucomaSymptoms(hasSymptoms: Boolean) {
        _glaucomaSymptoms.value = hasSymptoms
        deriveAlerts()
        validate()
    }

    fun setDrSymptoms(hasSymptoms: Boolean) {
        _drSymptoms.value = hasSymptoms
        deriveAlerts()
        validate()
    }

    fun setPresbyopiaSymptoms(hasSymptoms: Boolean) {
        _presbyopiaSymptoms.value = hasSymptoms
        deriveAlerts()
        validate()
    }

    fun setTrachomaStatus(status: String) {
        _trachomaStatus.value = status
        deriveAlerts()
        validate()
    }

    fun setCornealDiseaseType(type: String) {
        _cornealDiseaseType.value = type
        deriveAlerts()
        validate()
    }

    fun setVitaminADeficiency(hasDeficiency: Boolean) {
        _vitaminADeficiency.value = hasDeficiency
        deriveAlerts()
        validate()
    }

    fun setInjuryTypes(types: List<String>) {
        _injuryTypes.value = types
        clearRemovedInjuryFields(types)
        updateInjuryFieldVisibility(types)
        deriveInjuryAlerts()
        validate()
    }

    fun setForeignBodyRemoval(value: String) {
        _foreignBodyRemoval.value = value
        deriveInjuryAlerts()
        validate()
    }

    fun setChemicalExposure(value: Boolean) {
        _chemicalExposure.value = value
        deriveInjuryAlerts()
        validate()
    }

    private fun clearRemovedInjuryFields(currentInjuryTypes: List<String>) {
        if (!currentInjuryTypes.contains(DropdownConst.INJURY_MECHANICAL_FOREIGN_BODY)) {
            _foreignBodyRemoval.value = null
            _showForeignBodyAlert.value = false
        }
        if (!currentInjuryTypes.contains(DropdownConst.INJURY_CHEMICAL)) {
            _chemicalExposure.value = null
            _showChemicalExposureAlert.value = false
        }
    }

    private fun updateInjuryFieldVisibility(injuryTypes: List<String>) {
        _showForeignBodyRemovalField.value = injuryTypes.contains(DropdownConst.INJURY_MECHANICAL_FOREIGN_BODY)
        _showChemicalExposureField.value = injuryTypes.contains(DropdownConst.INJURY_CHEMICAL)
    }

    private fun deriveInjuryAlerts() {
        val injuryTypes = _injuryTypes.value ?: emptyList()
        _showForeignBodyAlert.value = injuryTypes.contains(DropdownConst.INJURY_MECHANICAL_FOREIGN_BODY) &&
                _foreignBodyRemoval.value == DropdownConst.FOREIGN_BODY_LODGED_IN_CORNEA
        // Alert fires only after the user has answered the "Chemical Exposure – Thorough Wash
        // Performed" Yes/No field (PRD: alert is conditional on the field, not on injury type alone).
        _showChemicalExposureAlert.value = injuryTypes.contains(DropdownConst.INJURY_CHEMICAL) &&
                _chemicalExposure.value == true
    }


    private fun clearRemovedConditionSubFields(currentConditions: List<String>) {
        if (!currentConditions.contains(DropdownConst.CONDITION_CATARACT)) {
            _cataractSymptoms.value = null
            _showCataractAlert.value = false
        }
        if (!currentConditions.contains(DropdownConst.CONDITION_GLAUCOMA)) {
            _glaucomaSymptoms.value = null
            _showGlaucomaAlert.value = false
        }
        if (!currentConditions.contains(DropdownConst.CONDITION_DIABETIC_RETINOPATHY)) {
            _drSymptoms.value = null
            _showDrAlert.value = false
        }
        if (!currentConditions.contains(DropdownConst.CONDITION_PRESBYOPIA)) {
            _presbyopiaSymptoms.value = null
            _showPresbyopiaAlert.value = false
        }
        if (!currentConditions.contains(DropdownConst.CONDITION_TRACHOMA)) {
            _trachomaStatus.value = null
            _showTrachomaAlert.value = false
        }
        if (!currentConditions.contains(DropdownConst.CONDITION_CORNEAL_DISEASE)) {
            _cornealDiseaseType.value = null
            _showCornealAlert.value = false
        }
        if (!currentConditions.contains(DropdownConst.CONDITION_DRY_EYE)) {
            _vitaminADeficiency.value = null
            _showVitaminAAlert.value = false
        }
    }

    private fun updateConditionSubFieldVisibility(conditions: List<String>) {
        _showCataractSubField.value = conditions.contains(DropdownConst.CONDITION_CATARACT)
        _showGlaucomaSubField.value = conditions.contains(DropdownConst.CONDITION_GLAUCOMA)
        _showDrSubField.value = conditions.contains(DropdownConst.CONDITION_DIABETIC_RETINOPATHY)
        _showPresbyopiaSubField.value = conditions.contains(DropdownConst.CONDITION_PRESBYOPIA)
        _showTrachomaSubField.value = conditions.contains(DropdownConst.CONDITION_TRACHOMA)
        _showCornealSubField.value = conditions.contains(DropdownConst.CONDITION_CORNEAL_DISEASE)
        _showVitaminASubField.value = conditions.contains(DropdownConst.CONDITION_DRY_EYE)
    }

    private fun deriveAlerts() {
        val conditions = _caseIdConditions.value ?: emptyList()
        _showCataractAlert.value = conditions.contains(DropdownConst.CONDITION_CATARACT) &&
                _cataractSymptoms.value == true
        _showGlaucomaAlert.value = conditions.contains(DropdownConst.CONDITION_GLAUCOMA) &&
                _glaucomaSymptoms.value == true
        _showDrAlert.value = conditions.contains(DropdownConst.CONDITION_DIABETIC_RETINOPATHY) &&
                _drSymptoms.value == true
        _showPresbyopiaAlert.value = conditions.contains(DropdownConst.CONDITION_PRESBYOPIA) &&
                _presbyopiaSymptoms.value == true
        val trachoma = _trachomaStatus.value
        _showTrachomaAlert.value = conditions.contains(DropdownConst.CONDITION_TRACHOMA) &&
                (trachoma == DropdownConst.TRACHOMA_SUSPECTED_ACTIVE ||
                        trachoma == DropdownConst.TRACHOMA_SUSPECTED_TT)
        _showCornealAlert.value = conditions.contains(DropdownConst.CONDITION_CORNEAL_DISEASE) &&
                !_cornealDiseaseType.value.isNullOrEmpty()
        _showVitaminAAlert.value = conditions.contains(DropdownConst.CONDITION_DRY_EYE) &&
                _vitaminADeficiency.value == true
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

        val conditions = _caseIdConditions.value ?: emptyList()
        updateConditionSubFieldVisibility(conditions)
        val injuryTypes = _injuryTypes.value ?: emptyList()
        updateInjuryFieldVisibility(injuryTypes)
    }

    enum class MissingField {
        IS_DIABETIC,
        SCREENING_PERFORMED,
        CHART_USED,
        DIST_VA_RIGHT,
        DIST_VA_LEFT,
        NEAR_VA,
        CASE_ID,
        CATARACT,
        GLAUCOMA,
        DR,
        PRESBYOPIA,
        TRACHOMA,
        CORNEAL,
        VITAMIN_A,
        INJURY_TYPE,
        FOREIGN_BODY_REMOVAL,
        CHEMICAL_EXPOSURE,
        NONE
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
            isInjuryTraumaReason(reason) ->
                ValidationResult(
                    fieldsValid = getMissingInjuryTraumaField() == MissingField.NONE,
                    alert = false,
                    caseIdByVA = false
                )
            reason == DropdownConst.REASON_SYMPTOMATIC -> {
                val validCaseId = if (isMando) caseIds.isNotEmpty() else true
                val subFieldsValid = areVisibleSubFieldsAnswered()
                ValidationResult(fieldsValid = validCaseId && subFieldsValid, alert = false, caseIdByVA = false)
            }
            reason == DropdownConst.screening && diabetic == true ->
                validateDiabeticPath(rightVA, leftVA)
            reason == DropdownConst.screening && diabetic == false ->
                validateNonDiabeticPath(rightVA, leftVA, nearVaValue)
            else ->
                ValidationResult(fieldsValid = false, alert = false, caseIdByVA = false)
        }

        _showVisualImpairmentAlert.value = result.alert
        _showCaseIdSection.value = (reason == DropdownConst.REASON_SYMPTOMATIC) || result.caseIdByVA
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

    private fun areVisibleSubFieldsAnswered(): Boolean {
        return getMissingSubField() == MissingField.NONE
    }

    private fun getMissingSubField(): MissingField {
        val conditions = _caseIdConditions.value ?: return MissingField.NONE
        if (conditions.contains(DropdownConst.CONDITION_CATARACT) && _cataractSymptoms.value == null) return MissingField.CATARACT
        if (conditions.contains(DropdownConst.CONDITION_GLAUCOMA) && _glaucomaSymptoms.value == null) return MissingField.GLAUCOMA
        if (conditions.contains(DropdownConst.CONDITION_DIABETIC_RETINOPATHY) && _drSymptoms.value == null) return MissingField.DR
        if (conditions.contains(DropdownConst.CONDITION_PRESBYOPIA) && _presbyopiaSymptoms.value == null) return MissingField.PRESBYOPIA
        if (conditions.contains(DropdownConst.CONDITION_TRACHOMA) && _trachomaStatus.value.isNullOrEmpty()) return MissingField.TRACHOMA
        if (conditions.contains(DropdownConst.CONDITION_CORNEAL_DISEASE) && _cornealDiseaseType.value.isNullOrEmpty()) return MissingField.CORNEAL
        if (conditions.contains(DropdownConst.CONDITION_DRY_EYE) && _vitaminADeficiency.value == null) return MissingField.VITAMIN_A
        return MissingField.NONE
    }

    fun getMissingMandatoryField(): MissingField {
        val reason = _reasonForVisit.value
        return when (reason) {
            DropdownConst.REASON_FIRST_AID_EYE_INJURY,
            DropdownConst.REASON_FIRST_AID_INJURY_TRAUMA -> getMissingInjuryTraumaField()
            DropdownConst.REASON_SYMPTOMATIC -> getMissingSymptomaticField()
            DropdownConst.screening -> getMissingScreeningField()
            else -> MissingField.NONE
        }
    }

    private fun getMissingInjuryTraumaField(): MissingField {
        val injuries = _injuryTypes.value ?: emptyList()
        if (injuries.isEmpty()) return MissingField.INJURY_TYPE
        if (injuries.contains(DropdownConst.INJURY_CHEMICAL) &&
            _chemicalExposure.value == null
        ) {
            return MissingField.CHEMICAL_EXPOSURE
        }
        return MissingField.NONE
    }

    private fun isInjuryTraumaReason(reason: String?): Boolean {
        return reason == DropdownConst.REASON_FIRST_AID_INJURY_TRAUMA ||
                reason == DropdownConst.REASON_FIRST_AID_EYE_INJURY
    }

    private fun getMissingSymptomaticField(): MissingField {
        val rightVA = _distVARight.value
        val leftVA = _distVALeft.value
        val caseIds = _caseIdConditions.value ?: emptyList()
        val isMando = isVisualImpairment(rightVA ?: "") || isVisualImpairment(leftVA ?: "") || isNearVAReduced(_nearVA.value)
        if (isMando && caseIds.isEmpty()) return MissingField.CASE_ID
        return getMissingSubField()
    }

    private fun getMissingScreeningField(): MissingField {
        val diabetic = _isDiabetic.value ?: return MissingField.IS_DIABETIC
        return if (diabetic) getMissingDiabeticField() else getMissingNonDiabeticField()
    }

    private fun getMissingDiabeticField(): MissingField {
        val screening = _isScreeningPerformed.value ?: return MissingField.SCREENING_PERFORMED
        if (!screening) return MissingField.NONE
        return getMissingDistanceVAField()
    }

    private fun getMissingNonDiabeticField(): MissingField {
        val chart = _chartUsed.value ?: return MissingField.CHART_USED
        return when (chart) {
            DropdownConst.CHART_SNELLENS -> getMissingDistanceVAField()
            DropdownConst.CHART_NEAR_VISION -> if (_nearVA.value == null) MissingField.NEAR_VA else MissingField.NONE
            else -> MissingField.NONE
        }
    }

    private fun getMissingDistanceVAField(): MissingField {
        if (_distVARight.value == null) return MissingField.DIST_VA_RIGHT
        if (_distVALeft.value == null) return MissingField.DIST_VA_LEFT
        return MissingField.NONE
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
                    cataractSymptoms = _cataractSymptoms.value
                    glaucomaSymptoms = _glaucomaSymptoms.value
                    diabeticRetinopathySymptoms = _drSymptoms.value
                    presbyopiaSymptoms = _presbyopiaSymptoms.value
                    trachomaStatus = _trachomaStatus.value
                    cornealDiseaseType = _cornealDiseaseType.value
                    vitaminADeficiency = _vitaminADeficiency.value

                    val injuryTypesVal = _injuryTypes.value
                    injuryType = if (!injuryTypesVal.isNullOrEmpty()) {
                        try { moshiAdapter.toJson(injuryTypesVal) } catch (e: Exception) {
                            Timber.e(e, "Error converting injuryTypes to JSON")
                            null
                        }
                    } else null
                    foreignBodyRemoval = _foreignBodyRemoval.value
                    chemicalExposure = _chemicalExposure.value

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
