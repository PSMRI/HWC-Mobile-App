package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.utils.ImgUtils
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.helpers.getWeeksOfPregnancy
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import java.util.concurrent.TimeUnit

class PregnantWomanAncAbortionDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val appContext = context.applicationContext

    private lateinit var registration: PregnantWomanRegistrationCache

    private val visitDate = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.visit_date),
        required = true,
        max = System.currentTimeMillis()
    )
    private val weekOfPregnancy = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.weeks_of_pregnancy),
        required = false
    )
    private val abortionDate = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.abortion_date),
        required = false
    )
    private val abortionType = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.abortion_type),
        required = false
    )
    private val abortionFacility = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.facility_place_of_abortion),
        required = false
    )
    private val serialNoAsPerAdmission = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.serial_no_as_per_admission_evacuation_register),
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED,
        etMaxLength = 10
    )
    private val methodOfTermination = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.method_of_termination),
        required = true,
        entries = resources.getStringArray(R.array.anc_method_of_termination)
    )
    private val terminationDoneBy = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.termination_done_by),
        required = true,
        entries = resources.getStringArray(R.array.anc_termination_done_by)
    )
    private val planningHeadline = FormElement(
        id = 25,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.what_family_planning_method_has_been_chosen_after_the_abortion),
        required = false
    )
    private val isPaiucd = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = " ",
        required = false,
        hasDependants = true,
        orientation = 1,
        entries = resources.getStringArray(R.array.abortion_isplaning_array)
    )
    private val isYesOrNo = FormElement(
        id = 23,
        inputType = InputType.RADIO,
        title = " ",
        required = true,
        hasDependants = true,
        orientation = 1,
        entries = resources.getStringArray(R.array.anc_confirmation_array)
    )
    private val dateOfSterilization = FormElement(
        id = 24,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date_of_sterilisation),
        required = true,
        max = System.currentTimeMillis()
    )
    private val remarks = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.remarks),
        required = false,
        etMaxLength = 100
    )
    private val abortionDischargeSummaryImg1 = FormElement(
        id = 21,
        inputType = InputType.FILE_UPLOAD,
        title = resources.getString(R.string.abortion_discharge_summary_1),
        required = false
    )
    private val abortionDischargeSummaryImg2 = FormElement(
        id = 22,
        inputType = InputType.FILE_UPLOAD,
        title = resources.getString(R.string.abortion_discharge_summary_2),
        required = false
    )

    suspend fun setUpPage(
        registrationRecord: PregnantWomanRegistrationCache,
        abortionAnc: PregnantWomanAncCache
    ) {
        registration = registrationRecord
        val list = mutableListOf(
            visitDate,
            weekOfPregnancy,
            abortionDate,
            abortionType,
            abortionFacility,
            serialNoAsPerAdmission,
            methodOfTermination,
            terminationDoneBy,
            planningHeadline,
            isPaiucd,
            remarks,
            abortionDischargeSummaryImg1,
            abortionDischargeSummaryImg2
        )

        val now = System.currentTimeMillis()
        val oneYearMillis = TimeUnit.DAYS.toMillis(365)
        abortionDate.min = registration.lmpDate + TimeUnit.DAYS.toMillis(5 * 7 + 1)
        abortionDate.max = minOf(now, registration.lmpDate + TimeUnit.DAYS.toMillis(21 * 7))

        visitDate.min = abortionAnc.abortionDate ?: (now - oneYearMillis)
        dateOfSterilization.min = abortionAnc.abortionDate

        visitDate.value = abortionAnc.visitDate?.let { getDateFromLong(it) }
        val week = getWeeksOfPregnancy(abortionAnc.ancDate, registration.lmpDate)
        weekOfPregnancy.value = week.toString()
        abortionType.value = abortionAnc.abortionType
        abortionFacility.value = abortionAnc.abortionFacility
        abortionDate.value = abortionAnc.abortionDate?.let { getDateFromLong(it) }
        serialNoAsPerAdmission.value = abortionAnc.serialNo
        methodOfTermination.value = abortionAnc.methodOfTermination
        terminationDoneBy.value = abortionAnc.terminationDoneBy
        remarks.value = abortionAnc.remarks
        abortionDischargeSummaryImg1.value = abortionAnc.abortionImg1
        abortionDischargeSummaryImg2.value = abortionAnc.abortionImg2
        isPaiucd.value = when (abortionAnc.isPaiucdId) {
            1 -> isPaiucd.entries?.getOrNull(0)
            2 -> isPaiucd.entries?.getOrNull(1)
            else -> abortionAnc.isPaiucd
        }
        if (abortionAnc.isPaiucdId != null && abortionAnc.isPaiucdId != 0) {
            list.add(list.indexOf(isPaiucd) + 1, isYesOrNo)
            isYesOrNo.value = if (abortionAnc.isYesOrNo == true) {
                isYesOrNo.entries?.getOrNull(0)
            } else {
                isYesOrNo.entries?.getOrNull(1)
            }
            if (abortionAnc.isPaiucdId == 2 && abortionAnc.isYesOrNo == true) {
                list.add(list.indexOf(isYesOrNo) + 1, dateOfSterilization)
                dateOfSterilization.value = abortionAnc.dateSterilisation?.let { getDateFromLong(it) }
            }
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            isPaiucd.id -> {
                isYesOrNo.value = null
                triggerDependants(
                    source = isPaiucd,
                    addItems = listOf(isYesOrNo),
                    removeItems = listOf(dateOfSterilization)
                )
            }

            isYesOrNo.id -> {
                val paiucdSecondOption = (isPaiucd.entries?.indexOf(isPaiucd.value ?: "") ?: -1) == 1
                if (isYesOrNo.value == "Yes" && paiucdSecondOption) {
                    triggerDependants(
                        source = isYesOrNo,
                        addItems = listOf(dateOfSterilization),
                        removeItems = emptyList()
                    )
                } else {
                    triggerDependants(
                        source = isYesOrNo,
                        addItems = emptyList(),
                        removeItems = listOf(dateOfSterilization)
                    )
                }
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PregnantWomanAncCache).let { cache ->
            cache.visitDate = visitDate.value?.let { getLongFromDate(it) }
            cache.lmpDate = registration.lmpDate
            cache.serialNo = serialNoAsPerAdmission.value
            cache.methodOfTermination = methodOfTermination.value
            cache.methodOfTerminationId = methodOfTermination.entries?.indexOf(methodOfTermination.value ?: "")?.takeIf { it >= 0 }
            cache.terminationDoneBy = terminationDoneBy.value
            cache.terminationDoneById = terminationDoneBy.entries?.indexOf(terminationDoneBy.value ?: "")?.takeIf { it >= 0 }
            cache.isPaiucd = isPaiucd.value
            cache.isPaiucdId = (isPaiucd.entries?.indexOf(isPaiucd.value ?: "") ?: -1) + 1
            cache.isYesOrNo = isYesOrNo.entries?.indexOf(isYesOrNo.value ?: "") == 0
            cache.remarks = remarks.value
            cache.dateSterilisation = dateOfSterilization.value?.let { getLongFromDate(it) }
            cache.abortionImg1 = abortionDischargeSummaryImg1.value?.let {
                ImgUtils.encodeLocalImageValueForUpload(appContext, it)
            }
            cache.abortionImg2 = abortionDischargeSummaryImg2.value?.let {
                ImgUtils.encodeLocalImageValueForUpload(appContext, it)
            }
        }
    }

    fun getWeeksOfPregnancyIndex(): Int = getIndexById(weekOfPregnancy.id)

    fun setImageUriToFormElement(formId: Int, uri: String) {
        when (formId) {
            21 -> abortionDischargeSummaryImg1.value = uri
            22 -> abortionDischargeSummaryImg2.value = uri
        }
    }

    fun getImageFieldIndex(formId: Int): Int = getIndexById(formId)

    fun getAbortionImageFieldValue(formId: Int): String? {
        return when (formId) {
            abortionDischargeSummaryImg1.id -> abortionDischargeSummaryImg1.value
            abortionDischargeSummaryImg2.id -> abortionDischargeSummaryImg2.value
            else -> null
        }
    }
}
