package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.helpers.setToStartOfTheDay
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.getDateStrFromLong
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PncFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var visit: Int = 0
    private var dateOfDelivery: Long = 0L

    private val pncPeriod = FormElement(
        id = 1,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_period),
//        entries = resources.getStringArray(R.array.pnc_period_array),
        arrayId = -1,
        required = true,
        hasDependants = false
    )

    private val visitDate = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.pnc_visit_date),
        arrayId = -1,
        required = true,
        hasDependants = false
    )

    private val ifaTabsGiven = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_ifa_tabs_given),
        required = false,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        max = 400,
        min = 0,
    )

    private val anyContraceptionMethod = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pnc_any_contraception_method),
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true
    )

    private val contraceptionMethod = FormElement(
        id = 5,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_contraception_method),
        entries = resources.getStringArray(R.array.pnc_contraception_method_array),
        required = false,
        hasDependants = true
    )

    private val otherPpcMethod = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_ppc_method),
        required = true,
        hasDependants = false
    )

    private val motherDangerSign = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_mother_danger_sign),
        entries = resources.getStringArray(R.array.pnc_mother_danger_sign_array),
        required = false,
        hasDependants = true
    )

    private val otherDangerSign = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_danger_sign),
        required = true,
        hasDependants = false
    )

    private val referralFacility = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_referral_facility),
        entries = resources.getStringArray(R.array.pnc_referral_facility_array),
        required = false,
        hasDependants = false
    )

    private val motherDeath = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pnc_mother_death),
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true,
    )

    private val deathDate = FormElement(
        id = 11,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.pnc_death_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val causeOfDeath = FormElement(
        id = 12,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_death_cause),
        entries = resources.getStringArray(R.array.pnc_death_cause_array),
        required = true,
        hasDependants = true,
    )

    private val otherDeathCause = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_death_cause),
        required = true,
        hasDependants = false
    )

    private val placeOfDeath = FormElement(
        id = 14,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_death_place),
        entries = resources.getStringArray(R.array.pnc_death_place_array),
        required = true,
        hasDependants = false,
    )

    private val remarks = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_remarks),
        required = false,
        hasDependants = false
    )

    private val deliveryDate = FormElement(
        id = 16,
        inputType = InputType.TEXT_VIEW,
        title = "Delivery Date",
        required = false,
        hasDependants = false
    )

    suspend fun setUpPage(
        visitNumber: Int,
        ben: PatientDisplay?,
        deliveryOutcomeCache: DeliveryOutcomeCache,
        previousPnc: PNCVisitCache?,
        saved: PNCVisitCache?
    ) {
        val list = mutableListOf(
            deliveryDate,
            pncPeriod,
            visitDate,
            ifaTabsGiven,
            anyContraceptionMethod,
            motherDangerSign,
            referralFacility,
            motherDeath,
            remarks
        )
        dateOfDelivery = deliveryOutcomeCache.dateOfDelivery!!
        deliveryDate.value = deliveryOutcomeCache.getDateStringFromLong(dateOfDelivery)
        deathDate.min = dateOfDelivery
        deathDate.max = System.currentTimeMillis()
        motherDeath.value = motherDeath.entries!!.last()
        val daysSinceDeliveryMillis = Calendar.getInstance()
            .setToStartOfTheDay().timeInMillis - deliveryOutcomeCache.dateOfDelivery!!.let {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            cal.setToStartOfTheDay()
            cal.timeInMillis
        }
        val daysSinceDelivery = TimeUnit.MILLISECONDS.toDays(daysSinceDeliveryMillis)
        pncPeriod.entries =
            listOf(
                1,
                3,
                7,
                14,
                21,
                28,
                42
            ).filter { if (daysSinceDelivery == 0L) it <= 1 else it <= daysSinceDelivery }
                .filter { it > (previousPnc?.pncPeriod ?: 0) }
                .map { "Day $it" }.toTypedArray()

        saved?.let {
            pncPeriod.value = "Day ${it.pncPeriod}"
            visitDate.value = getDateFromLong(it.pncDate)
            ifaTabsGiven.value = it.ifaTabsGiven?.toString()
            anyContraceptionMethod.value = it.anyContraceptionMethod?.let {
                if (it)
                    anyContraceptionMethod.entries!!.first()
                else
                    anyContraceptionMethod.entries!!.last()
            }
            if (it.anyContraceptionMethod == true) {
                list.add(list.indexOf(anyContraceptionMethod) + 1, contraceptionMethod)
            }
            contraceptionMethod.value = it.contraceptionMethod
            if (it.contraceptionMethod == contraceptionMethod.entries!!.last()) {
                list.add(list.indexOf(contraceptionMethod) + 1, otherPpcMethod)
            }
            otherPpcMethod.value = it.otherPpcMethod
            motherDangerSign.value = it.motherDangerSign
            if (it.motherDangerSign == motherDangerSign.entries!!.last()) {
                list.add(list.indexOf(motherDangerSign) + 1, otherDangerSign)
            }
            otherDangerSign.value = it.otherDangerSign
            referralFacility.value = it.referralFacility
            motherDeath.value =
                if (it.motherDeath) motherDeath.entries!!.first() else motherDeath.entries!!.last()
            if (it.motherDeath) {
                deathDate.value = getDateStrFromLong(it.deathDate)
                causeOfDeath.value = it.causeOfDeath
                otherDeathCause.value = it.otherDeathCause
                placeOfDeath.value = it.placeOfDeath
                list.addAll(
                    list.indexOf(motherDeath) + 1,
                    listOf(deathDate, causeOfDeath, placeOfDeath)
                )
                if (causeOfDeath.value == causeOfDeath.entries!!.last())
                    list.add(list.indexOf(causeOfDeath) + 1, otherDeathCause)
            }
            remarks.value = it.remarks
        }

//        pncPeriod.entries = pncPeriod.entries!!.
//        if (saved == null) {
//            dateOfDelivery.value = Dataset.getDateFromLong(System.currentTimeMillis())
//            dateOfDischarge.value = Dataset.getDateFromLong(System.currentTimeMillis())
//        } else {
//            list = mutableListOf(
//                dateOfDelivery,
//                timeOfDelivery,
//                placeOfDelivery,
//                typeOfDelivery,
//                hadComplications,
////                complication,
////                causeOfDeath,
////                otherCauseOfDeath,
////                otherComplication,
//                deliveryOutcome,
//                liveBirth,
//                stillBirth,
//                dateOfDischarge,
//                timeOfDischarge,
//                isJSYBenificiary
//            )
//            dateOfDelivery.value = Dataset.getDateFromLong(saved.dateOfDelivery)
//            timeOfDelivery.value = saved.timeOfDelivery
//            placeOfDelivery.value = saved.placeOfDelivery
//            typeOfDelivery.value = saved.typeOfDelivery
//            hadComplications.value = if (saved.hadComplications == true) "Yes" else "No"
//            complication.value = saved.complication
//            causeOfDeath.value = saved.causeOfDeath
//            otherCauseOfDeath.value = saved.otherCauseOfDeath
//            otherComplication.value = saved.otherComplication
//            deliveryOutcome.value = saved.deliveryOutcome.toString()
//            liveBirth.value = saved.liveBirth.toString()
//            stillBirth.value = saved.stillBirth.toString()
//            dateOfDischarge.value = Dataset.getDateFromLong(saved.dateOfDischarge)
//            timeOfDischarge.value = saved.timeOfDischarge
//            isJSYBenificiary.value = if (saved.isJSYBenificiary == true) "Yes" else "No"
//        }
//        ben?.let {
//            dateOfDelivery.min = it.regDate
//        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            pncPeriod.id -> {
                visitDate.inputType = InputType.DATE_PICKER
                visitDate.value = null
                val today = Calendar.getInstance().setToStartOfTheDay().timeInMillis
                when (val visitNumber = pncPeriod.value!!.substring(4).toInt()) {
                    1 -> {
                        visitDate.min = minOf(today, dateOfDelivery)
                        visitDate.max = minOf(
                            today,
                            dateOfDelivery + TimeUnit.DAYS.toMillis(1)
                        )
                    }

                    3 -> {
                        visitDate.min = minOf(today, dateOfDelivery + TimeUnit.DAYS.toMillis(3))
                        visitDate.max = minOf(
                            today,
                            dateOfDelivery + TimeUnit.DAYS.toMillis(3)
                        )
                    }

                    7 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(7) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(7) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    14 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(14) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(14) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    21 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(21) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(21) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    28 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(28) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(28) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    42 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(42) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(42) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    else -> throw IllegalStateException("Illegal PNC Date $visitNumber")
                }
                return -1
            }

            ifaTabsGiven.id -> validateIntMinMax(ifaTabsGiven)
            anyContraceptionMethod.id -> triggerDependants(
                source = anyContraceptionMethod,
                passedIndex = index,
                triggerIndex = 0,
                target = contraceptionMethod,
                targetSideEffect = listOf(otherPpcMethod)
            )

            contraceptionMethod.id -> triggerDependants(
                source = contraceptionMethod,
                passedIndex = index,
                triggerIndex = contraceptionMethod.entries!!.lastIndex,
                target = otherPpcMethod,
            )

            motherDangerSign.id ->
                triggerDependants(
                    source = motherDangerSign,
                    passedIndex = index,
                    triggerIndex = motherDangerSign.entries!!.lastIndex,
                    target = otherDangerSign
                )

            motherDeath.id -> {
                triggerDependants(
                    source = motherDeath,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = listOf(deathDate, causeOfDeath, placeOfDeath),
                    targetSideEffect = listOf(otherDeathCause)
                )
            }

            causeOfDeath.id -> {
                triggerDependants(
                    source = causeOfDeath,
                    passedIndex = index,
                    triggerIndex = causeOfDeath.entries!!.lastIndex,
                    target = otherDeathCause
                )
            }

//
            else -> -1
        }
    }

//    private fun validateMaxDeliveryOutcome() : Int {
//        if(!liveBirth.value.isNullOrEmpty() && !stillBirth.value.isNullOrEmpty() &&
//            !deliveryOutcome.value.isNullOrEmpty() && deliveryOutcome.errorText.isNullOrEmpty()) {
//            if(deliveryOutcome.value!!.toInt() != liveBirth.value!!.toInt() + stillBirth.value!!.toInt()) {
//                deliveryOutcome.errorText = "Outcome of Delivery should equal to sum of Live and Still births"
//            }
//        }
//        return -1
//    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PNCVisitCache).let { form ->
            form.pncPeriod = pncPeriod.value!!.substring(4).toInt()
            form.pncDate = getLongFromDate(visitDate.value!!)
            form.ifaTabsGiven = ifaTabsGiven.value?.takeIf { it.isNotEmpty() }?.toInt()
            form.anyContraceptionMethod =
                anyContraceptionMethod.value?.let { it == anyContraceptionMethod.entries!!.first() }
            form.contraceptionMethod = contraceptionMethod.value?.takeIf { it.isNotEmpty() }
            form.otherPpcMethod = otherPpcMethod.value?.takeIf { it.isNotEmpty() }
            form.motherDangerSign = motherDangerSign.value?.takeIf { it.isNotEmpty() }
            form.otherDangerSign = otherDangerSign.value?.takeIf { it.isNotEmpty() }
            form.referralFacility = referralFacility.value?.takeIf { it.isNotEmpty() }
            form.motherDeath =
                motherDeath.value?.let { it == motherDeath.entries!!.first() } ?: false
            form.deathDate = deathDate.value?.let { getLongFromDate(it) }
            form.causeOfDeath = causeOfDeath.value?.takeIf { it.isNotEmpty() }
            form.otherDeathCause = otherDeathCause.value?.takeIf { it.isNotEmpty() }
            form.placeOfDeath = placeOfDeath.value?.takeIf { it.isNotEmpty() }
            form.remarks = remarks.value?.takeIf { it.isNotEmpty() }
        }
    }
}