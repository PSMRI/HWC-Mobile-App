package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType


class NoseDiagnosisDataset(
    private val context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: NoseDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null

    private val optionYes = context.getString(R.string.yes)
    private val optionNo = context.getString(R.string.no)
    private val foreignBodyOptions = context.resources.getStringArray(R.array.nose_foreign_body_options)

    private val FOREIGN_BODY_ANTERIOR = 0
    private val FOREIGN_BODY_POSTERIOR = 1
    private val FOREIGN_BODY_NONE = 2

    private fun foreignBodyStringToIndex(value: String?): Int? {
        return when (value) {
            foreignBodyOptions.getOrNull(FOREIGN_BODY_ANTERIOR) -> FOREIGN_BODY_ANTERIOR
            foreignBodyOptions.getOrNull(FOREIGN_BODY_POSTERIOR) -> FOREIGN_BODY_POSTERIOR
            foreignBodyOptions.getOrNull(FOREIGN_BODY_NONE) -> FOREIGN_BODY_NONE
            else -> null
        }
    }

    private fun indexToForeignBodyString(index: Int?): String? {
        return when (index) {
            FOREIGN_BODY_ANTERIOR -> foreignBodyOptions.getOrNull(FOREIGN_BODY_ANTERIOR)
            FOREIGN_BODY_POSTERIOR -> foreignBodyOptions.getOrNull(FOREIGN_BODY_POSTERIOR)
            FOREIGN_BODY_NONE -> foreignBodyOptions.getOrNull(FOREIGN_BODY_NONE)
            else -> null
        }
    }

    /* -------------------- FORM ELEMENTS -------------------- */

    private val difficultyBreathing = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = context.getString(R.string.difficulty_in_breathing),
        entries = arrayOf(optionYes, optionNo),
        trueIndex = 0,
        falseIndex = 1,
        required = true,
        hasAlertError = true
    )

    private val openMouthBreathing = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = context.getString(R.string.open_mouth_breathing),
        entries = arrayOf(optionYes, optionNo),
        trueIndex = 0,
        falseIndex = 1,
        required = false,
        hasAlertError = true
    )
    private val noseBleed = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = context.getString(R.string.nose_bleed_title),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasDependants = true
    )

    private val systolicBP = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = context.getString(R.string.systolic_bp_title),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        hasAlertError = true

    )

    private val diastolicBP = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = context.getString(R.string.diastolic_bp_title),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        hasAlertError = true

    )

    private val foreignBodyNose = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.foreign_body_nose_title),
        entries = foreignBodyOptions,
        required = false,
        hasAlertError = true

    )

    private val sinusitis = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.sinusitis_title),
        entries = arrayOf(context.getString(R.string.sinusitis_with_pain), optionNo),
        required = false,
        hasAlertError = true
    )



    suspend fun setUpPage(savedRecord: NoseDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)
        val list = mutableListOf<FormElement>()
        list.add(difficultyBreathing)
        list.add(openMouthBreathing)
        list.add(noseBleed)

        if (noseBleed.value == optionYes) {
            list.add(systolicBP)
            list.add(diastolicBP)
        }

        list.addAll(
            listOf(
                foreignBodyNose,
                sinusitis
            )
        )

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

            difficultyBreathing.id -> {
                difficultyBreathing.booleanValue = when (index) {
                    difficultyBreathing.trueIndex -> true
                    difficultyBreathing.falseIndex -> false
                    else -> null
                }
                if (difficultyBreathing.booleanValue == true) {
                    onShowAlert?.invoke(
                        context.getString(R.string.difficulty_in_breathing_alert)
                    )
                }
                -1
            }

            openMouthBreathing.id -> {
                openMouthBreathing.booleanValue = when (index) {
                    openMouthBreathing.trueIndex -> true
                    openMouthBreathing.falseIndex -> false
                    else -> null
                }
                if (openMouthBreathing.booleanValue == true) {
                    onShowAlert?.invoke(
                        context.getString(R.string.open_mouth_breathing_alert)
                    )
                }
                -1
            }
            noseBleed.id -> {
                if (index == 0) {
                    triggerDependants(
                        source = noseBleed,
                        addItems = listOf(systolicBP, diastolicBP),
                        removeItems = emptyList()
                    )
                } else {
                    systolicBP.value = null
                    diastolicBP.value = null
                    triggerDependants(
                        source = noseBleed,
                        addItems = emptyList(),
                        removeItems = listOf(systolicBP, diastolicBP)
                    )
                }
                noseBleed.id
            }

            systolicBP.id -> {
                systolicBP.value?.toIntOrNull()?.let {
                    if (it > 120) {
                        onShowAlert?.invoke(
                            context.getString(R.string.nose_bleed_alert_systolic_bp)
                        )
                    }
                }
                -1
            }

            diastolicBP.id -> {
                diastolicBP.value?.toIntOrNull()?.let {
                    if (it > 80) {
                        onShowAlert?.invoke(
                            context.getString(R.string.nose_bleed_alert_diastolic_bp)
                        )
                    }
                }
                -1
            }

            foreignBodyNose.id -> {
                if (index == FOREIGN_BODY_POSTERIOR) {
                    onShowAlert?.invoke(
                        context.getString(R.string.foreign_body_posterior_alert)
                    )
                }
                -1
            }

            sinusitis.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(
                        context.getString(R.string.sinusitis_alert)
                    )
                }
                -1
            }
            else -> -1
        }
    }


    private fun createDefaultCache(): NoseDiagnosisAssessment {
        return NoseDiagnosisAssessment(
            patientID = "",
            benVisitNo = null
        )
    }

    private fun populateFromCache(cache: NoseDiagnosisAssessment) {
        difficultyBreathing.booleanValue = cache.difficultyBreathing
        difficultyBreathing.value = when (cache.difficultyBreathing) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        openMouthBreathing.booleanValue = cache.openMouthBreathing
        openMouthBreathing.value = when (cache.openMouthBreathing) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        noseBleed.value = when (cache.noseBleed) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        systolicBP.value = cache.systolicBP?.toString()
        diastolicBP.value = cache.diastolicBP?.toString()


        // Convert stored index to display string for UI
        val foreignBodyIndex = cache.foreignBodyNose?.toIntOrNull()
        foreignBodyNose.value = indexToForeignBodyString(foreignBodyIndex)

        sinusitis.value = when (cache.sinusitis) {
            true -> context.getString(R.string.sinusitis_with_pain)
            false -> optionNo
            else -> null
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as NoseDiagnosisAssessment).let {
            it.difficultyBreathing = difficultyBreathing.booleanValue
            it.openMouthBreathing = openMouthBreathing.booleanValue
            val hasNoseBleed: Boolean? = when (noseBleed.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

            it.noseBleed = hasNoseBleed

            it.systolicBP = if (hasNoseBleed == true) {
                systolicBP.value?.toIntOrNull()
            } else null

            it.diastolicBP = if (hasNoseBleed == true) {
                diastolicBP.value?.toIntOrNull()
            } else null
            // Store stable index as string instead of localized display text
            it.foreignBodyNose = foreignBodyStringToIndex(foreignBodyNose.value)?.toString()
            it.sinusitis = when (sinusitis.value) {
                context.getString(R.string.sinusitis_with_pain) -> true
                optionNo -> false
                else -> null
            }

        }
    }
}