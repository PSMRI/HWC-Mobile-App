package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.helpers.getWeeksOfPregnancy
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import java.util.concurrent.TimeUnit
import kotlin.math.min

class PregnantWomanAncVisitDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var lastAncVisitDate: Long = 0L

    private lateinit var regis: PregnantWomanRegistrationCache

    private val ancDate = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = "ANC Date",
        required = true,
        hasDependants = true,
    )
    private val weekOfPregnancy = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = "Weeks of Pregnancy",
        required = false,
    )

    private val ancVisit = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = "ANC Period",
        required = true,
    )
    private val isAborted = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Abortion If Any",
        entries = arrayOf("No", "Yes"),
        required = false,
        hasDependants = true
    )
    private val abortionType = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Abortion Type",
        entries = arrayOf("Induced", "Spontaneous"),
        required = true,
        hasDependants = true
    )
    private val abortionFacility = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Facility",
        entries = arrayOf("Govt. Hospital", "Pvt. Hospital"),
        required = true,
        hasDependants = true
    )
    private val abortionDate = FormElement(
        id = 7,
        inputType = InputType.DATE_PICKER,
        title = "Abortion Date",
        required = true,
        max = System.currentTimeMillis(),
    )

    private val weight = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = "Weight of PW (Kg) at time Registration",
        arrayId = -1,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 30,
        max = 200
    )
    private val bp = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = "BP of PW – Systolic/ Diastolic (mm Hg) ",
//        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 7,
        required = false,
    )
//    private val bpDiastolic = FormElement(
//        id = 10,
//        inputType = InputType.EDIT_TEXT,
//        title = "BP of PW (mm Hg) – Diastolic",
//        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
//        etMaxLength = 3,
//        required = false,
//        min = 30,
//        max = 200
//    )
//    private val bpSystolicReq = FormElement(
//        id = 119,
//        inputType = InputType.EDIT_TEXT,
//        title = "BP of PW (mm Hg) – Systolic",
//        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
//        etMaxLength = 3,
//        required = true,
//        min = 50,
//        max = 300
//    )
//    private val bpDiastolicReq = FormElement(
//        id = 120,
//        inputType = InputType.EDIT_TEXT,
//        title = "BP of PW (mm Hg) – Diastolic",
//        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
//        etMaxLength = 3,
//        required = true,
//        min = 30,
//        max = 200
//    )

    private val pulseRate = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        title = "Pulse Rate",
        required = false,
    )

    private val hb = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = "HB (gm/dl)",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 4,
        minDecimal = 2.0,
        maxDecimal = 15.0,
        required = false,
    )

    private val fundalHeight = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = "Fundal Height / Size of the Uterus weeks",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        required = false,
    )
    private val urineAlbumin = FormElement(
        id = 14,
        inputType = InputType.DROPDOWN,
        title = "Urine Albumin",
        entries = arrayOf("Negative", "Trace", "+", "++", "+++"),
        required = true,
        hasDependants = true
    )
    private val randomBloodSugarTest = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = "Random Blood Sugar Test",
        entries = arrayOf("Not Done", "Done"),
        required = false,
    )
    private val dateOfTTOrTd1 = FormElement(
        id = 16,
        inputType = InputType.DATE_PICKER,
        title = "Date of Td TT (1st Dose)",
        required = false,
        hasDependants = true,
        max = System.currentTimeMillis(),
    )
    private val dateOfTTOrTd2 = FormElement(
        id = 17,
        inputType = InputType.DATE_PICKER,
        title = "Date of Td TT (2nd Dose)",
        required = false,
        max = System.currentTimeMillis(),
    )
    private val dateOfTTOrTdBooster = FormElement(
        id = 18,
        inputType = InputType.DATE_PICKER,
        title = "Date of Td TT (Boooster Dose)",
        required = false,
        hasDependants = true,

        max = System.currentTimeMillis(),
    )
    private val numFolicAcidTabGiven = FormElement(
        id = 19,
        inputType = InputType.EDIT_TEXT,
        title = "No. of Folic Acid Tabs given",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        required = false,
        min = 0,
        max = 60
    )
    private val numIfaAcidTabGiven = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = "No. of IFA Tabs given",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 0,
        max = 400
    )
    private val anyHighRisk = FormElement(
        id = 21,
        inputType = InputType.RADIO,
        title = "Any High Risk conditions",
        entries = arrayOf("No", "Yes"),
        required = false,
        hasDependants = true
    )
    private val highRiskCondition = FormElement(
        id = 22, inputType = InputType.DROPDOWN, title = "High Risk Conditions", entries = arrayOf(
            "NONE",
            "HIGH BP (SYSTOLIC>=140 AND OR DIASTOLIC >=90mmHg)",
            "CONVULSIONS",
            "VAGINAL BLEEDING",
            "FOUL SMELLING DISCHARGE",
            "SEVERE ANAEMIA (HB<7 gm/dl)",
            "DIABETES",
            "TWINS",
            "OTHER",
        ), required = false, hasDependants = true
    )
    private val otherHighRiskCondition = FormElement(
        id = 23,
        inputType = InputType.EDIT_TEXT,
        title = "Any other High Risk conditions",
        required = true,
    )
    private val highRiskReferralFacility = FormElement(
        id = 24,
        inputType = InputType.DROPDOWN,
        title = "Referral Facility",
        entries = arrayOf(
            "Primary Health Centre",
            "Community Health Centre",
            "District Hospital",
            "Other Private Hospital",
        ),
        required = false,
    )
    private val hrpConfirm = FormElement(
        id = 25, inputType = InputType.RADIO, title = "Is HRP Confirmed?", entries = arrayOf(
            "No", "Yes",
        ), required = false, hasDependants = true
    )
    private val hrpConfirmedBy = FormElement(
        id = 26,
        inputType = InputType.DROPDOWN,
        title = "Who had identified as HRP?",
        entries = arrayOf(
            "ANM",
            "CHO",
            "PHC – MO",
            "Specialist at Higher Facility",
        ),
        required = true,
    )
    private val maternalDeath = FormElement(
        id = 27, inputType = InputType.RADIO, title = "Maternal Death", entries = arrayOf(
            "No", "Yes",
        ), required = false, hasDependants = true
    )
    private val maternalDeathProbableCause = FormElement(
        id = 28,
        inputType = InputType.DROPDOWN,
        title = "Probable Cause of Death",
        entries = arrayOf(
            "ECLAMPSIA", "HAEMORRHAGE", "HIGH FEVER", "ABORTION", "OTHER"
        ),
        required = true,
        hasDependants = true
    )
    private val otherMaternalDeathProbableCause = FormElement(
        id = 29,
        inputType = InputType.EDIT_TEXT,
        title = "Other Death Cause",
        required = true,
    )
    private val maternalDateOfDeath = FormElement(
        id = 30,
        inputType = InputType.DATE_PICKER,
        title = "Death Date",
        required = true,
        max = System.currentTimeMillis(),
    )

    private val deliveryDone = FormElement(
        id = 31,
        inputType = InputType.RADIO,
        title = "Has the pregnant woman delivered?",
        entries = arrayOf(
            "Yes", "No",
        ),
        required = false,
    )

    private val lmpDate = FormElement(
        id = 32,
        inputType = InputType.TEXT_VIEW,
        title = "LMP Date",
        required = false,
        hasDependants = false
    )

    private val bloodSugarFasting = FormElement(
        id = 33,
        inputType = InputType.EDIT_TEXT,
        title = "Blood Sugar (Fasting) mg/dL",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = true,
        min = 40,
        max = 400,
        hasDependants = true
    )

    private val urineSugar = FormElement(
        id = 34,
        inputType = InputType.DROPDOWN,
        title = "Urine Sugar",
        entries = arrayOf("Negative", "Trace", "+", "++", "+++"),
        required = true,
        hasDependants = true
    )

    private val fetalHeartRate = FormElement(
        id = 35,
        inputType = InputType.EDIT_TEXT,
        title = "Fetal Heart Rate (FHR) bpm",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 5,
        required = true,
        minDecimal = 40.0,
        maxDecimal = 200.0,
        hasDependants = true
    )

    private val calciumGiven = FormElement(
        id = 36,
        inputType = InputType.EDIT_TEXT,
        title = "Calcium Given",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 0,
        max = 400
    )

    private val dangerSigns = FormElement(
        id = 37,
        inputType = InputType.CHECKBOXES,
        title = "Danger Signs",
        entries = arrayOf(
            "None",
            "Vaginal Bleeding",
            "Swelling of hands, feet or face",
            "Severe Headache",
            "Blurred Vision",
            "Convulsions/ seizures",
            "Severe abdominal pain",
            "Fever > 38°C",
            "Painful urination/ Burning",
            "Reduced fetal movement",
            "Vaginal fluid leakage",
            "Persistent vomiting",
            "Breathlessness/ chest pain"
        ),
        required = true,
        hasDependants = true
    )

    private val counsellingProvided = FormElement(
        id = 38,
        inputType = InputType.RADIO,
        title = "Counselling Provided",
        entries = arrayOf("No", "Yes"),
        required = false,
        hasDependants = true
    )

    private val counsellingTopics = FormElement(
        id = 39,
        inputType = InputType.CHECKBOXES,
        title = "Which counselling was provided?",
        entries = arrayOf(
            "Nutrition",
            "Birth Preparedness and Complication readiness",
            "Identification of danger signs",
            "Medication compliance (IFA/ Calcium/ other medications)",
            "Immunization",
            "Hygiene and Infection Prevention"
        ),
        required = true
    )

    private val nextAncVisitDate = FormElement(
        id = 40,
        inputType = InputType.DATE_PICKER,
        title = "Next ANC Visit Date",
        required = true
    )

    private var toggleBp = false

    fun resetBpToggle() {
        toggleBp = false
    }

    fun triggerBpToggle() = toggleBp


    suspend fun setUpPage(
        visitNumber: Int,
        ben: PatientDisplay?,
        regis: PregnantWomanRegistrationCache,
        lastAnc: PregnantWomanAncCache?,
        saved: PregnantWomanAncCache?
    ) {
        this.regis = regis
        val list = mutableListOf(
            lmpDate,
            ancDate,
            weekOfPregnancy,
            ancVisit,
            isAborted,
            weight,
            bp,
            pulseRate,
            hb,
            bloodSugarFasting,
            fundalHeight,
            urineAlbumin,
            urineSugar,
            fetalHeartRate,
            randomBloodSugarTest,
            dateOfTTOrTd1,
            dateOfTTOrTd2,
            dateOfTTOrTdBooster,
            numFolicAcidTabGiven,
            numIfaAcidTabGiven,
            calciumGiven,
            anyHighRisk,
            dangerSigns,
            highRiskReferralFacility,
            hrpConfirm,
            counsellingProvided,
            maternalDeath,
            nextAncVisitDate
        )
        lmpDate.value = this.regis.getDateStringFromLong(this.regis.lmpDate)
        abortionDate.min = regis.lmpDate + TimeUnit.DAYS.toMillis(5 * 7 + 1)
        dateOfTTOrTd1.min = abortionDate.min
        dateOfTTOrTdBooster.min = abortionDate.min
        abortionDate.max =
            minOf(System.currentTimeMillis(), regis.lmpDate + TimeUnit.DAYS.toMillis(21 * 7))
        dateOfTTOrTd1.max = abortionDate.max
        dateOfTTOrTd2.max = abortionDate.max
        dateOfTTOrTdBooster.max = abortionDate.max

        // Next ANC Visit Date range (> today, <= EDD)
        nextAncVisitDate.min = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        nextAncVisitDate.max = getEddFromLmp(regis.lmpDate)

        if (lastAnc == null)
            list.remove(dateOfTTOrTd2)
        setUpTdX()
        ben?.let {
            ancDate.min =
                regis.lmpDate + TimeUnit.DAYS.toMillis(7 * Konstants.minAnc1Week.toLong() + 1)
            ancVisit.entries = arrayOf("1", "2", "3", "4")
            lastAnc?.let { last ->
                ancDate.min = last.ancDate + TimeUnit.DAYS.toMillis(4 * 7)
                ancVisit.entries = arrayOf(2, 3, 4).filter {
                    it > last.visitNumber
                }.map { it.toString() }.toTypedArray()

                lastAncVisitDate = last.ancDate
            }
            ancDate.max =
                minOf(getEddFromLmp(regis.lmpDate), System.currentTimeMillis())
            ancDate.value = getDateFromLong(ancDate.max!!)
            maternalDateOfDeath.min =
                maxOf(regis.lmpDate, lastAncVisitDate) + TimeUnit.DAYS.toMillis(1)
            maternalDateOfDeath.max =
                minOf(getEddFromLmp(regis.lmpDate), System.currentTimeMillis())
        }

//        ancDate.value = getDateFromLong(System.currentTimeMillis())
        if (saved == null) {
            weekOfPregnancy.value = ancDate.value?.let {
                val long = getLongFromDate(it)
                val weeks = getWeeksOfPregnancy(long, regis.lmpDate)
                if (weeks >= Konstants.minWeekToShowDelivered) {
                    list.add(deliveryDone)
                }
                if (weeks <= 12) {
                    list.remove(fundalHeight)
                    list.remove(numIfaAcidTabGiven)
                } else {
                    list.remove(numFolicAcidTabGiven)
                }
                // Disable Calcium up to 14 weeks
                if (weeks <= 14) {
                    list.remove(calciumGiven)
                }
                weeks.toString()
            }
        }

        ancVisit.value = visitNumber.toString()

        saved?.let { savedAnc ->

            val woP = getWeeksOfPregnancy(savedAnc.ancDate, regis.lmpDate)
            if (woP <= 12) {
                list.remove(fundalHeight)
                list.remove(numIfaAcidTabGiven)
            } else {
                list.remove(numFolicAcidTabGiven)
            }
            if (woP >= Konstants.minWeekToShowDelivered) {
                if (!list.contains(deliveryDone)) list.add(deliveryDone)
            }
            ancDate.value = getDateFromLong(savedAnc.ancDate)
            weekOfPregnancy.value = woP.toString()
            isAborted.value =
                if (savedAnc.isAborted) isAborted.entries!!.last() else isAborted.entries!!.first()
            if (savedAnc.isAborted) {
                abortionType.value = abortionType.getStringFromPosition(savedAnc.abortionTypeId)
                abortionFacility.value =
                    abortionFacility.getStringFromPosition(savedAnc.abortionFacilityId)
                abortionDate.value = savedAnc.abortionDate?.let { getDateFromLong(it) }
                list.addAll(
                    list.indexOf(isAborted) + 1,
                    listOf(abortionType, abortionFacility, abortionDate)
                )
            }
            weight.value = savedAnc.weight?.toString()
            bp.value =
                if (savedAnc.bpSystolic == null || savedAnc.bpDiastolic == null) null else "${savedAnc.bpSystolic}/${savedAnc.bpDiastolic}"
//            bpDiastolic.value = savedAnc.bpDiastolic?.toString()
            pulseRate.value = savedAnc.pulseRate
            hb.value = savedAnc.hb?.toString()
            fundalHeight.value = savedAnc.fundalHeight?.toString()
            urineAlbumin.value = urineAlbumin.getStringFromPosition(savedAnc.urineAlbuminId)
            randomBloodSugarTest.value =
                randomBloodSugarTest.getStringFromPosition(savedAnc.randomBloodSugarTestId)
            dateOfTTOrTd1.value = regis.tt1?.let { getDateFromLong(it) }
            dateOfTTOrTd2.value = regis.tt2?.let { getDateFromLong(it) }
            dateOfTTOrTdBooster.value = regis.ttBooster?.let { getDateFromLong(it) }
            numFolicAcidTabGiven.value = savedAnc.numFolicAcidTabGiven.toString()
            numIfaAcidTabGiven.value = savedAnc.numIfaAcidTabGiven.toString()
            savedAnc.anyHighRisk?.let {
                anyHighRisk.value =
                    if (it) anyHighRisk.entries!!.last() else anyHighRisk.entries!!.first()
                if (it) {
                    highRiskCondition.value =
                        highRiskCondition.getStringFromPosition(savedAnc.highRiskId)
                    list.add(list.indexOf(anyHighRisk) + 1, highRiskCondition)
                    if (highRiskCondition.value == highRiskCondition.entries!!.last()) {
                        otherHighRiskCondition.value = savedAnc.otherHighRisk
                        list.add(list.indexOf(highRiskCondition) + 1, otherHighRiskCondition)
                    }
                }
            }

            highRiskReferralFacility.value =
                highRiskReferralFacility.getStringFromPosition(savedAnc.referralFacilityId)
            hrpConfirm.value =
                savedAnc.hrpConfirmed?.let { if (it) hrpConfirm.entries!!.last() else hrpConfirm.entries!!.first() }
            if (savedAnc.hrpConfirmed == true) {
                hrpConfirmedBy.value =
                    hrpConfirmedBy.getStringFromPosition(savedAnc.hrpConfirmedById)
                list.add(list.indexOf(hrpConfirm) + 1, hrpConfirmedBy)
            }
            savedAnc.maternalDeath?.let {
                maternalDeath.value =
                    if (it) maternalDeath.entries!!.last() else maternalDeath.entries!!.first()
                if (it) {
                    maternalDeathProbableCause.value =
                        maternalDeathProbableCause.getStringFromPosition(savedAnc.maternalDeathProbableCauseId)
                    maternalDateOfDeath.value =
                        savedAnc.deathDate?.let { it1 -> getDateFromLong(it1) }
                    list.addAll(
                        list.indexOf(maternalDeath) + 1,
                        listOf(maternalDeathProbableCause, maternalDateOfDeath)
                    )
                    otherMaternalDeathProbableCause.value =
                        savedAnc.otherMaternalDeathProbableCause
                    if (maternalDeathProbableCause.value == maternalDeathProbableCause.entries!!.last()) list.add(
                        list.indexOf(maternalDeathProbableCause) + 1,
                        otherMaternalDeathProbableCause
                    )
                }
            }
            deliveryDone.value =
                if (savedAnc.pregnantWomanDelivered == true) deliveryDone.entries!!.first() else deliveryDone.entries!!.last()
        }
        setUpPage(list)

    }

    private fun setUpTdX() {
        if (regis.ttBooster != null) {
            dateOfTTOrTdBooster.value = getDateFromLong(regis.ttBooster!!)
            dateOfTTOrTd1.inputType = InputType.TEXT_VIEW
            dateOfTTOrTd2.inputType = InputType.TEXT_VIEW
            dateOfTTOrTdBooster.inputType = InputType.TEXT_VIEW
        } else if (regis.tt1 == null) {
            dateOfTTOrTd2.inputType = InputType.TEXT_VIEW
        } else {
            dateOfTTOrTd1.value = getDateFromLong(regis.tt1!!)
            dateOfTTOrTdBooster.inputType = InputType.TEXT_VIEW
            dateOfTTOrTd1.inputType = InputType.TEXT_VIEW
            if (regis.tt2 == null) {
                dateOfTTOrTd2.min = regis.tt1!! + TimeUnit.DAYS.toMillis(28)
                dateOfTTOrTd2.max = min(System.currentTimeMillis(), getEddFromLmp(regis.lmpDate))
            } else {
                dateOfTTOrTd2.value = getDateFromLong(regis.tt2!!)
                dateOfTTOrTd2.inputType = InputType.TEXT_VIEW
            }
        }
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            ancDate.id -> {
                ancDate.value?.let {
                    val long = getLongFromDate(it)
                    val weeks = 2
//                    val weeks = getWeeksOfPregnancy(long, regis.lmpDate)
                    val listChanged = if (weeks >= Konstants.minWeekToShowDelivered) {
                        triggerDependants(
                            source = maternalDeath,
                            addItems = listOf(deliveryDone),
                            removeItems = emptyList(),
                        )
                    } else {
                        triggerDependants(
                            source = maternalDeath,
                            addItems = emptyList(),
                            removeItems = listOf(deliveryDone),
                        )
                    }
                    weekOfPregnancy.value = weeks.toString()
                    val calcVisitNumber = when (weeks) {
                        in Konstants.minAnc1Week..Konstants.maxAnc1Week -> 1
                        in Konstants.minAnc2Week..Konstants.maxAnc2Week -> 2
                        in Konstants.minAnc3Week..Konstants.maxAnc3Week -> 3
                        in Konstants.minAnc4Week..Konstants.maxAnc4Week -> 4
                        else -> 0
                    }
                    if (ancVisit.entries?.contains(calcVisitNumber.toString()) == true) {
                        ancVisit.value = calcVisitNumber.toString()
                        val listChanged2 = if (weeks <= 12)
                            triggerDependants(
                                source = ancVisit,
                                addItems = listOf(numFolicAcidTabGiven),
                                removeItems = listOf(fundalHeight, numIfaAcidTabGiven),
                                position = getIndexById(dateOfTTOrTdBooster.id) + 1
                            )
                        else {
                            triggerDependants(
                                source = ancVisit,
                                removeItems = listOf(numFolicAcidTabGiven),
                                addItems = listOf(fundalHeight),
                                position = getIndexById(hb.id) + 1
                            )
                            triggerDependants(
                                source = ancVisit,
                                removeItems = listOf(),
                                addItems = listOf(numIfaAcidTabGiven),
                                position = getIndexById(dateOfTTOrTdBooster.id) + 1
                            )

                        }
                        return if (listChanged >= 0 || listChanged2 >= 0)
                            1
                        else
                            -1
                    }
                    return listChanged
                }
                -1
            }

            ancVisit.id -> {
                if (ancVisit.value == "1")
                    triggerDependants(
                        source = ancVisit,
                        addItems = listOf(numFolicAcidTabGiven),
                        removeItems = listOf(fundalHeight, numIfaAcidTabGiven),
                        position = getIndexById(dateOfTTOrTdBooster.id) + 1
                    )
                else {
                    triggerDependants(
                        source = ancVisit,
                        removeItems = listOf(numFolicAcidTabGiven),
                        addItems = listOf(fundalHeight),
                        position = getIndexById(hb.id) + 1
                    )
                    triggerDependants(
                        source = ancVisit,
                        removeItems = listOf(),
                        addItems = listOf(numIfaAcidTabGiven),
                        position = getIndexById(dateOfTTOrTdBooster.id) + 1
                    )

                }
            }

            isAborted.id -> triggerDependants(
                source = isAborted,
                passedIndex = index,
                triggerIndex = 1,
                target = listOf(abortionType, abortionDate),
                targetSideEffect = listOf(abortionFacility)
            )

            abortionType.id -> triggerDependants(
                source = abortionType,
                passedIndex = index,
                triggerIndex = 0,
                target = abortionFacility,
            )

            dateOfTTOrTd1.id -> {
                if (dateOfTTOrTd1.value == null)
                    dateOfTTOrTdBooster.inputType = InputType.DATE_PICKER
                else
                    dateOfTTOrTdBooster.inputType = InputType.TEXT_VIEW
                -1
            }

            dateOfTTOrTdBooster.id -> {
                if (dateOfTTOrTdBooster.value == null) {
                    dateOfTTOrTd1.inputType = InputType.DATE_PICKER
                } else {
                    dateOfTTOrTd1.inputType = InputType.TEXT_VIEW
                }

                -1
            }

            bp.id -> validateForBp(bp)

            weight.id -> validateIntMinMax(weight)

            hb.id -> {
                validateDoubleUpto1DecimalPlaces(hb)
                if (hb.errorText == null) validateDoubleMinMax(hb)
                -1
            }

            numFolicAcidTabGiven.id -> validateIntMinMax(numFolicAcidTabGiven)
            numIfaAcidTabGiven.id -> validateIntMinMax(numIfaAcidTabGiven)
            anyHighRisk.id -> triggerDependants(
                source = anyHighRisk,
                passedIndex = index,
                triggerIndex = 1,
                target = highRiskCondition,
                targetSideEffect = listOf(otherHighRiskCondition)
            ).also {
                hb.value?.takeIf { it.isNotEmpty() && hb.errorText == null }?.toDouble()?.let {
                    if (it < 7)
                        highRiskCondition.value = highRiskCondition.entries!![5]
                }
                bp.value?.takeIf { it.isNotEmpty() && hb.errorText == null }?.let {
                    val sys = it.substringBefore("/").toInt()
                    val dia = it.substringAfter("/").toInt()
                    if (sys > 140 || dia > 90) {
                        highRiskCondition.value = highRiskCondition.entries!![1]
                    }
                }
                if (highRiskCondition.value == null)
                    highRiskCondition.value = highRiskCondition.entries!!.first()
            }

            highRiskCondition.id -> triggerDependants(
                source = highRiskCondition,
                passedIndex = index,
                triggerIndex = highRiskCondition.entries!!.lastIndex,
                target = otherHighRiskCondition,
            )

            otherHighRiskCondition.id -> {
                validateEmptyOnEditText(otherHighRiskCondition)
                validateAllAlphabetsSpaceOnEditText(otherHighRiskCondition)
            }

            hrpConfirm.id -> triggerDependants(
                source = hrpConfirm,
                passedIndex = index,
                triggerIndex = 1,
                target = hrpConfirmedBy,
            )

            maternalDeath.id -> triggerDependants(
                source = maternalDeath,
                passedIndex = index,
                triggerIndex = 1,
                target = listOf(maternalDeathProbableCause, maternalDateOfDeath),
                targetSideEffect = listOf(otherMaternalDeathProbableCause)
            )

            maternalDeathProbableCause.id -> triggerDependants(
                source = maternalDeathProbableCause,
                passedIndex = index,
                triggerIndex = maternalDeathProbableCause.entries!!.lastIndex,
                target = otherMaternalDeathProbableCause,
            )

            otherMaternalDeathProbableCause.id -> {
                validateEmptyOnEditText(otherMaternalDeathProbableCause)
                validateAllAlphabetsSpaceOnEditText(otherMaternalDeathProbableCause)
            }

            // NEW FIELD VALIDATIONS

            bloodSugarFasting.id -> {
                validateIntMinMax(bloodSugarFasting)
                bloodSugarFasting.value?.takeIf { it.isNotEmpty() && bloodSugarFasting.errorText == null }?.toInt()?.let { bsValue ->
                    if (bsValue > 95) {
                        if (anyHighRisk.value != anyHighRisk.entries?.last()) {
                            anyHighRisk.value = anyHighRisk.entries?.last()
                        }
                        else {
                            if (highRiskCondition.value == null || highRiskCondition.value == highRiskCondition.entries?.first()) {
                                highRiskCondition.value = highRiskCondition.entries!![6]
                            }
                        }
                    }
                }
                -1
            }

            urineSugar.id -> {
                urineSugar.value?.let { value ->
                    val riskLevels = arrayOf("+", "++", "+++")
                    if (value in riskLevels) {
                        if (anyHighRisk.value != anyHighRisk.entries?.last()) {
                            anyHighRisk.value = anyHighRisk.entries?.last()
                        }
                        if (highRiskCondition.value == null || highRiskCondition.value == highRiskCondition.entries?.first()) {
                            highRiskCondition.value = highRiskCondition.entries!![6]
                        }
                    }
                }
                -1
            }

            fetalHeartRate.id -> {
                validateDoubleUpto1DecimalPlaces(fetalHeartRate)
                if (fetalHeartRate.errorText == null) validateDoubleMinMax(fetalHeartRate)

                fetalHeartRate.value?.takeIf { it.isNotEmpty() && fetalHeartRate.errorText == null }?.toDouble()?.let { fhrValue ->
                    if( (fhrValue < 110.0 || fhrValue > 160.0) && (anyHighRisk.value != anyHighRisk.entries?.last())) {
                            anyHighRisk.value = anyHighRisk.entries?.last()
                    }
                }
                -1
            }

            urineAlbumin.id -> {
                urineAlbumin.value?.let { value ->
                    val riskLevels = arrayOf("+", "++", "+++")
                    if( (value in riskLevels) &&(anyHighRisk.value != anyHighRisk.entries?.last()) ){
                            anyHighRisk.value = anyHighRisk.entries?.last()
                    }
                }
                -1
            }

            calciumGiven.id -> validateIntMinMax(calciumGiven)

            dangerSigns.id -> {
                dangerSigns.value?.let { value ->
                    if (value != "None") {
                        if (anyHighRisk.value != anyHighRisk.entries?.last()) {
                            anyHighRisk.value = anyHighRisk.entries?.last()
                        }
                        when (value) {
                            "Vaginal Bleeding" -> highRiskCondition.value = highRiskCondition.entries!![3]
                            "Convulsions/ seizures" -> highRiskCondition.value = highRiskCondition.entries!![2]
                            else -> {
                                if (highRiskCondition.value == null || highRiskCondition.value == highRiskCondition.entries?.first()) {
                                    highRiskCondition.value = highRiskCondition.entries!!.last()
                                }
                            }
                        }
                    }
                }
                -1
            }

            counsellingProvided.id -> triggerDependants(
                source = counsellingProvided,
                passedIndex = index,
                triggerIndex = 1,
                target = counsellingTopics,
            )

            else -> -1
        }

    }

    fun getIndexOfTd1() = getIndexById(dateOfTTOrTd1.id)
    fun getIndexOfTd2() = getIndexById(dateOfTTOrTd2.id)
    fun getIndexOfTdBooster() = getIndexById(dateOfTTOrTdBooster.id)


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PregnantWomanAncCache).let { cache ->
            cache.visitNumber = ancVisit.value!!.toInt()
            cache.ancDate = getLongFromDate(ancDate.value)
            cache.isAborted = isAborted.value == isAborted.entries!!.last()
            cache.abortionType = abortionType.value
            cache.abortionTypeId = abortionType.getPosition()
            cache.abortionFacility = abortionFacility.value
            cache.abortionFacilityId = abortionFacility.getPosition()
            cache.abortionDate = abortionDate.value?.let { getLongFromDate(it) }
            cache.weight = weight.value?.toInt()
            cache.bpSystolic = bp.value?.takeIf { it.isNotEmpty() }?.substringBefore("/")?.toInt()
            cache.bpDiastolic = bp.value?.takeIf { it.isNotEmpty() }?.substringAfter("/")?.toInt()
            cache.pulseRate = pulseRate.value?.takeIf { it.isNotEmpty() }
            cache.hb = hb.value?.toDouble()
            cache.fundalHeight = fundalHeight.value?.toInt()
            cache.urineAlbumin = urineAlbumin.value
            cache.urineAlbuminId = urineAlbumin.getPosition()
            cache.randomBloodSugarTest = randomBloodSugarTest.value
            cache.randomBloodSugarTestId = randomBloodSugarTest.getPosition()
            updateRegistrationForTdX()
            cache.numFolicAcidTabGiven = numFolicAcidTabGiven.value?.toInt() ?: 0
            cache.numIfaAcidTabGiven = numIfaAcidTabGiven.value?.toInt() ?: 0
            anyHighRisk.value?.let {
                cache.anyHighRisk = it == anyHighRisk.entries!!.last()
            }
            cache.highRisk = highRiskCondition.value
            cache.highRiskId = highRiskCondition.getPosition()
            cache.otherHighRisk = otherHighRiskCondition.value
            cache.referralFacility = highRiskReferralFacility.value
            cache.referralFacilityId = highRiskReferralFacility.getPosition()
            cache.hrpConfirmed = hrpConfirm.value?.let { it == hrpConfirm.entries!!.last() }
            cache.hrpConfirmedBy = hrpConfirmedBy.value
            cache.hrpConfirmedById = hrpConfirmedBy.getPosition()
            cache.maternalDeath = maternalDeath.value == maternalDeath.entries!!.last()
            cache.maternalDeathProbableCause = maternalDeathProbableCause.value
            cache.maternalDeathProbableCauseId = maternalDeathProbableCause.getPosition()
            cache.otherMaternalDeathProbableCause = otherMaternalDeathProbableCause.value
            cache.deathDate = maternalDateOfDeath.value?.let { getLongFromDate(it) }
            deliveryDone.value?.let {
                cache.pregnantWomanDelivered = it == deliveryDone.entries!!.first()
            }


            cache.bloodSugarFasting = bloodSugarFasting.value?.toInt()
            cache.urineSugar = urineSugar.value
            cache.urineSugarId = urineSugar.getPosition()
            cache.fetalHeartRate = fetalHeartRate.value?.toDouble()
            cache.calciumGiven = calciumGiven.value?.toInt() ?: 0
            cache.dangerSigns = dangerSigns.value
            cache.dangerSignsId = dangerSigns.getPosition()
            cache.counsellingProvided = counsellingProvided.value == counsellingProvided.entries?.last()
            cache.counsellingTopics = counsellingTopics.value
            cache.counsellingTopicsId = counsellingTopics.getPosition()
            cache.nextAncVisitDate = nextAncVisitDate.value?.let { getLongFromDate(it) }
        }
    }

    private fun updateRegistrationForTdX() {
        if (dateOfTTOrTd1.value.isNullOrBlank() || dateOfTTOrTd2.value.isNullOrBlank() || dateOfTTOrTdBooster.value.isNullOrBlank())
            return
        else {
            val td1 = if (dateOfTTOrTd1.inputType == InputType.DATE_PICKER) getLongFromDate(
                dateOfTTOrTd1.value
            ) else null
            val td2 = if (dateOfTTOrTd2.inputType == InputType.DATE_PICKER) getLongFromDate(
                dateOfTTOrTd2.value
            ) else null
            val tdBooster =
                if (dateOfTTOrTdBooster.inputType == InputType.DATE_PICKER) getLongFromDate(
                    dateOfTTOrTdBooster.value
                ) else null
            if (td1 == null && td2 == null && tdBooster == null)
                return
        }
        regis.tt1 = dateOfTTOrTd1.value?.let { getLongFromDate(it) }
        regis.tt2 = dateOfTTOrTd2.value?.let { getLongFromDate(it) }
        regis.ttBooster = dateOfTTOrTdBooster.value?.let { getLongFromDate(it) }
        regis.updatedDate = System.currentTimeMillis()
        if (regis.processed != "N") regis.processed = "U"
        regis.syncState = SyncState.UNSYNCED
    }

    fun getWeeksOfPregnancy(): Int = getIndexById(weekOfPregnancy.id)

//    fun updateBenRecordToDelivered(it: BenRegCache) {
//        it.genDetails?.apply {
//            reproductiveStatus =
//                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[2]
//            reproductiveStatusId = 3
//        }
//        if (it.processed != "N") it.processed = "U"
//        it.syncState = SyncState.UNSYNCED
//    }

//    fun updateBenRecordToEligibleCouple(it: BenRegCache) {
//        it.genDetails?.apply {
//            reproductiveStatus =
//                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[0]
//            reproductiveStatusId = 1
//        }
//        if (it.processed != "N") it.processed = "U"
//        it.syncState = SyncState.UNSYNCED
//    }

}