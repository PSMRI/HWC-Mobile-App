package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class DeliveryOutcomeDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        @Throws(Exception::class)
        fun getOneMonthLater(deliveryDate: String?): String {
            val sdf = SimpleDateFormat("dd-MM-yyyy")
            val date: Date = sdf.parse(deliveryDate)

            val calendar: Calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.MONTH, 1) // Add 1 month

            return sdf.format(calendar.time)
        }
    }

    private val dateOfDelivery = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_delivery_date),
        arrayId = -1,
        required = true,
        hasDependants = true
    )

    private val timeOfDelivery = FormElement(
        id = 2,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.do_delivery_time),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private var placeOfDelivery = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_delivery_place),
        entries = resources.getStringArray(R.array.do_place_of_delivery_array),
        required = false,
        hasDependants = false
    )

    private var typeOfDelivery = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_delivery_type),
        entries = resources.getStringArray(R.array.do_type_of_delivery_array),
        required = true,
        hasDependants = false
    )

    private var hadComplications = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_had_complication),
        entries = resources.getStringArray(R.array.do_had_complications_array),
        required = false,
        hasDependants = true
    )

    private var complication = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_delivery_complication),
        entries = resources.getStringArray(R.array.do_complications_array),
        required = true,
        hasDependants = true
    )

    private var causeOfDeath = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_death_cause),
        entries = resources.getStringArray(R.array.do_cause_of_death_array),
        required = true,
        hasDependants = true
    )

    private var otherCauseOfDeath = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_other_death_cause),
        required = true,
        hasDependants = false
    )

    private var otherComplication = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_other_delivery_complication),
        required = true,
        hasDependants = false
    )

    private var dateOfDeath = FormElement(
        id = 10,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_death_date),
        arrayId = -1,
        required = true,
        hasDependants = false
    )

    private var placeOfDeath = FormElement(
        id = 11,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_death_place),
        entries = resources.getStringArray(R.array.do_death_place_array),
        required = true,
        hasDependants = true
    )

    private var otherPlaceOfDeath = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_other_place_of_death),
        required = true,
        hasDependants = false
    )

    private var deliveryOutcome = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_delivery_outcome),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 1,
        min = 1,
        max = 4,
        hasDependants = true
    )

    private var liveBirth = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_live_birth),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 1,
        min = 0,
        max = 4,
        hasDependants = false
    )

    private var stillBirth = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_still_birth),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 1,
        min = 0,
        max = 4,
        hasDependants = false
    )

    private var dateOfDischarge = FormElement(
        id = 16,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_discharge_date),
        arrayId = -1,
        required = false,
        hasDependants = false
    )

    private var timeOfDischarge = FormElement(
        id = 17,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.do_discharge_time),
        arrayId = -1,
        required = false,
        hasDependants = false
    )

    private var isJSYBeneficiary = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_is_jsy_beneficiary),
        entries = resources.getStringArray(R.array.do_is_jsy_beneficiary_array),
        required = false,
        hasDependants = false
    )

    suspend fun setUpPage(
        deliveryOutcomeCache: DeliveryOutcomeCache?,
        patientID: String
    ) {
        val list = mutableListOf<FormElement>()

        deliveryOutcomeCache?.let { cache ->
            cache.dateOfDelivery?.let {
                dateOfDelivery.value = getDateFromLong(it)
            }
            cache.timeOfDelivery?.let {
                timeOfDelivery.value = it
            }
            cache.placeOfDelivery?.let {
                placeOfDelivery.value = it
            }
            cache.typeOfDelivery?.let {
                typeOfDelivery.value = it
            }
            cache.hadComplications?.let {
                hadComplications.value = if (it) "Yes" else "No"
            }
            cache.complication?.let {
                complication.value = it
            }
            cache.causeOfDeath?.let {
                causeOfDeath.value = it
            }
            cache.otherCauseOfDeath?.let {
                otherCauseOfDeath.value = it
            }
            cache.otherComplication?.let {
                otherComplication.value = it
            }
            cache.deliveryOutcome?.let {
                deliveryOutcome.value = it.toString()
            }
            cache.liveBirth?.let {
                liveBirth.value = it.toString()
            }
            cache.stillBirth?.let {
                stillBirth.value = it.toString()
            }
            cache.dateOfDischarge?.let {
                dateOfDischarge.value = getDateFromLong(it)
            }
            cache.timeOfDischarge?.let {
                timeOfDischarge.value = it
            }
            cache.isJSYBenificiary?.let {
                isJSYBeneficiary.value = if (it) "Yes" else "No"
            }
            cache.dateOfDeath?.let {
                dateOfDeath.value = it
            }
            cache.placeOfDeath?.let {
                placeOfDeath.value = it
            }
            cache.otherPlaceOfDeath?.let {
                otherPlaceOfDeath.value = it
            }
        }

        list.add(dateOfDelivery)
        list.add(timeOfDelivery)
        list.add(placeOfDelivery)
        list.add(typeOfDelivery)
        list.add(hadComplications)

        if (hadComplications.value == "Yes") {
            list.add(complication)
            if (complication.value == "DEATH") {
                list.add(causeOfDeath)
                if (causeOfDeath.value == "Other") {
                    list.add(otherCauseOfDeath)
                }
                list.add(dateOfDeath)
                list.add(placeOfDeath)
                if (placeOfDeath.value == "Other") {
                    list.add(otherPlaceOfDeath)
                }
            } else if (complication.value == "ANY OTHER (SPECIFY)") {
                list.add(otherComplication)
            }
        }

        list.add(deliveryOutcome)
        list.add(liveBirth)
        list.add(stillBirth)
        list.add(dateOfDischarge)
        list.add(timeOfDischarge)
        list.add(isJSYBeneficiary)

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            hadComplications.id -> {
                triggerDependants(
                    source = hadComplications,
                    passedIndex = index,
                    triggerIndex = 0, // "Yes"
                    target = complication
                )
            }
            complication.id -> {
                // Handle DEATH complication
                val deathIndex = complication.entries?.indexOf("DEATH") ?: 6
                val otherIndex = complication.entries?.indexOf("ANY OTHER (SPECIFY)") ?: 7
                
                when (index) {
                    deathIndex -> {
                        triggerDependants(
                            source = complication,
                            removeItems = listOf(otherComplication),
                            addItems = listOf(causeOfDeath, dateOfDeath, placeOfDeath)
                        )
                    }
                    otherIndex -> {
                        triggerDependants(
                            source = complication,
                            removeItems = listOf(causeOfDeath, dateOfDeath, placeOfDeath, otherCauseOfDeath, otherPlaceOfDeath),
                            addItems = listOf(otherComplication)
                        )
                    }
                    else -> {
                        triggerDependants(
                            source = complication,
                            removeItems = listOf(causeOfDeath, dateOfDeath, placeOfDeath, otherCauseOfDeath, otherPlaceOfDeath, otherComplication),
                            addItems = listOf()
                        )
                    }
                }
            }
            causeOfDeath.id -> {
                val otherIndex = causeOfDeath.entries?.indexOf("Other") ?: 4
                triggerDependants(
                    source = causeOfDeath,
                    passedIndex = index,
                    triggerIndex = otherIndex,
                    target = otherCauseOfDeath
                )
            }
            placeOfDeath.id -> {
                val otherIndex = placeOfDeath.entries?.indexOf("Other") ?: 3
                triggerDependants(
                    source = placeOfDeath,
                    passedIndex = index,
                    triggerIndex = otherIndex,
                    target = otherPlaceOfDeath
                )
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as DeliveryOutcomeCache).let { cache ->
            cache.dateOfDelivery = getLongFromDate(dateOfDelivery.value)
            cache.timeOfDelivery = timeOfDelivery.value
            cache.placeOfDelivery = placeOfDelivery.value
            cache.typeOfDelivery = typeOfDelivery.value
            cache.hadComplications = hadComplications.value == "Yes"
            cache.complication = complication.value
            cache.causeOfDeath = causeOfDeath.value
            cache.otherCauseOfDeath = otherCauseOfDeath.value
            cache.otherComplication = otherComplication.value
            cache.deliveryOutcome = deliveryOutcome.value?.toIntOrNull()
            cache.liveBirth = liveBirth.value?.toIntOrNull()
            cache.stillBirth = stillBirth.value?.toIntOrNull()
            cache.dateOfDischarge = getLongFromDate(dateOfDischarge.value)
            cache.timeOfDischarge = timeOfDischarge.value
            cache.isJSYBenificiary = isJSYBeneficiary.value == "Yes"
            cache.dateOfDeath = dateOfDeath.value
            cache.placeOfDeath = placeOfDeath.value
            cache.otherPlaceOfDeath = otherPlaceOfDeath.value
            cache.isDeath = complication.value == "DEATH"
            cache.isDeathValue = if (complication.value == "DEATH") "Yes" else "No"
            cache.placeOfDeathId = placeOfDeath.entries?.indexOf(placeOfDeath.value ?: "")?.takeIf { it != -1 }
        }
    }
    
    fun getIndexOfMCP1() = 0  // Placeholder - file uploads not fully implemented
    fun getIndexOfMCP2() = 0  // Placeholder - file uploads not fully implemented
    fun getIndexOfIsjsyFileUpload() = 0  // Placeholder - file uploads not fully implemented
    
    fun setImageUriToFormElement(formId: Int, uri: android.net.Uri) {
        // Placeholder for file upload functionality
        // Would need to add FormElements for file uploads and set their values here
    }
}
