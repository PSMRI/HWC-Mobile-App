package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.Gender
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InputType

class InfantRegistrationDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var babyName = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.ir_baby_name),
        required = false,
        hasDependants = false
    )

    private var infantTerm = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.ir_infant_term),
        required = false,
        hasDependants = false
    )

    private var corticosteroidGiven = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_corticosteroid_given),
        entries = resources.getStringArray(R.array.ir_confirmation_array3),
        required = false,
        hasDependants = false
    )

    private var gender = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_gender),
        entries = resources.getStringArray(R.array.ir_gender_array),
        required = true,
        hasDependants = false
    )

    private var babyCriedAtBirth = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_baby_cried_at_birth),
        entries = resources.getStringArray(R.array.ir_confirmation_array1),
        required = true,
        hasDependants = true
    )

    private var resuscitation = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_resuscitation),
        entries = resources.getStringArray(R.array.ir_confirmation_array1),
        required = true,
        hasDependants = false
    )

    private var referred = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_referred),
        entries = resources.getStringArray(R.array.ir_confirmation_array2),
        required = false,
        hasDependants = false
    )

    private var hadBirthDefect = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_had_birth_defect),
        entries = resources.getStringArray(R.array.ir_confirmation_array2),
        required = false,
        hasDependants = true
    )

    private var birthDefect = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.ir_birth_defect),
        entries = resources.getStringArray(R.array.ir_birth_defect_array),
        required = false,
        hasDependants = true
    )

    private var otherDefect = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.ir_other_defect),
        required = false,
        hasDependants = false
    )

    private var weight = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.ir_weight),
        required = true,
        hasDependants = false,
        hasAlertError = true,
        etMaxLength = 5,
        min = 1,
        max = 7000,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL
    )

    private var breastFeedingStarted = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_breast_feeding_started),
        entries = resources.getStringArray(R.array.ir_confirmation_array1),
        required = true,
        hasDependants = false
    )

    private var isSncu = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_is_sncu),
        entries = resources.getStringArray(R.array.ir_confirmation_array1),
        required = false,
        hasDependants = true
    )

    // Note: File uploads for delivery discharge summaries would go here
    // They're commented out in FLW implementation and may need custom handling

    suspend fun setUpPage(
        infantRegCache: InfantRegCache?,
        deliveryOutcome: DeliveryOutcomeCache?,
        motherName: String,
        babyIndex: Int,
        lmpDate: Long?
    ) {
        val list = mutableListOf<FormElement>()

        // Set default baby name
        babyName.value = "Baby ${babyIndex + 1} of $motherName"

        // Calculate infant term based on LMP
        lmpDate?.let { lmp ->
            deliveryOutcome?.dateOfDelivery?.let { delivery ->
                val weeks = calculateWeeksOfPregnancy(lmp, delivery)
                infantTerm.value = if (weeks >= 37) "Full Term" else "Pre Term"
            }
        }

        infantRegCache?.let { cache ->
            cache.babyName?.let {
                babyName.value = it
            }
            cache.infantTerm?.let {
                infantTerm.value = it
            }
            cache.corticosteroidGiven?.let {
                corticosteroidGiven.value = it
            }
            cache.gender?.let {
                gender.value = it.name
            }
            cache.babyCriedAtBirth?.let {
                babyCriedAtBirth.value = if (it) "Yes" else "No"
            }
            cache.resuscitation?.let {
                resuscitation.value = if (it) "Yes" else "No"
            }
            cache.referred?.let {
                referred.value = it
            }
            cache.hadBirthDefect?.let {
                hadBirthDefect.value = it
            }
            cache.birthDefect?.let {
                birthDefect.value = it
            }
            cache.otherDefect?.let {
                otherDefect.value = it
            }
            cache.weight?.let {
                weight.value = it.toInt().toString()
            }
            cache.breastFeedingStarted?.let {
                breastFeedingStarted.value = if (it) "Yes" else "No"
            }
            cache.isSNCU?.let {
                isSncu.value = it
            }
        }

        list.add(babyName)
        list.add(infantTerm)
        list.add(corticosteroidGiven)
        list.add(gender)
        list.add(babyCriedAtBirth)

        if (babyCriedAtBirth.value == "No") {
            list.add(resuscitation)
        }

        list.add(referred)
        list.add(hadBirthDefect)

        if (hadBirthDefect.value == "Yes") {
            list.add(birthDefect)
            if (birthDefect.value == "Other") {
                list.add(otherDefect)
            }
        }

        list.add(weight)
        
        // Add weight validation alerts
        weight.value?.toIntOrNull()?.let { weightValue ->
            when {
                weightValue < 1000 -> {
                    weight.errorText = "Extremely Low Birth Weight (ELBW) - Immediate medical attention required"
                }
                weightValue < 1500 -> {
                    weight.errorText = "Very Low Birth Weight (VLBW) - Special care needed"
                }
                weightValue < 2500 -> {
                    weight.errorText = "Low Birth Weight (LBW) - Monitor closely"
                }
                weightValue > 4000 -> {
                    weight.errorText = "Macrosomia - Large baby, monitor for complications"
                }
                else -> {
                    weight.errorText = null
                }
            }
        }

        list.add(breastFeedingStarted)
        list.add(isSncu)

        // File uploads would be added here when SNCU is Yes

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            babyCriedAtBirth.id -> {
                val noIndex = babyCriedAtBirth.entries?.indexOf("No") ?: 1
                triggerDependants(
                    source = babyCriedAtBirth,
                    passedIndex = index,
                    triggerIndex = noIndex,
                    target = resuscitation
                )
            }
            hadBirthDefect.id -> {
                val yesIndex = hadBirthDefect.entries?.indexOf("Yes") ?: 0
                triggerDependants(
                    source = hadBirthDefect,
                    passedIndex = index,
                    triggerIndex = yesIndex,
                    target = birthDefect,
                    targetSideEffect = listOf(otherDefect)
                )
            }
            birthDefect.id -> {
                val otherIndex = birthDefect.entries?.indexOf("Other") ?: 6
                triggerDependants(
                    source = birthDefect,
                    passedIndex = index,
                    triggerIndex = otherIndex,
                    target = otherDefect
                )
            }
            isSncu.id -> {
                // File upload fields would be triggered here when implemented
                -1
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as InfantRegCache).let { cache ->
            cache.babyName = babyName.value
            cache.infantTerm = infantTerm.value
            cache.corticosteroidGiven = corticosteroidGiven.value
            cache.gender = when (gender.value) {
                "Male" -> Gender.MALE
                "Female" -> Gender.FEMALE
                "Ambiguous" -> Gender.TRANSGENDER
                else -> null
            }
            cache.babyCriedAtBirth = babyCriedAtBirth.value == "Yes"
            cache.resuscitation = resuscitation.value == "Yes"
            cache.referred = referred.value
            cache.hadBirthDefect = hadBirthDefect.value
            cache.birthDefect = birthDefect.value
            cache.otherDefect = otherDefect.value
            cache.weight = weight.value?.toDoubleOrNull()
            cache.breastFeedingStarted = breastFeedingStarted.value == "Yes"
            cache.isSNCU = isSncu.value
        }
    }

    private fun calculateWeeksOfPregnancy(lmpDate: Long, currentDate: Long): Int {
        val daysDiff = (currentDate - lmpDate) / (1000 * 60 * 60 * 24)
        return (daysDiff / 7).toInt()
    }
}
