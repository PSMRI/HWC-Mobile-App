package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport

class PsychosocialCaregiverSupportDataset(
    private val context: Context,
    currentLanguage: Languages
) : ReferralFollowUpDataset(context, currentLanguage) {

    private lateinit var cache: PsychosocialCaregiverSupport

    override val showCaseStatusOnlyWhenReferralNo = true

    private val optionYes = context.getString(R.string.yes_option)
    private val optionNo = context.getString(R.string.no_option)

    // ---------------- Psychosocial counselling ----------------
    private val psychosocialCounsellingProvided = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = context.getString(R.string.psychosocial_counselling_provided),
        entries = arrayOf(optionYes, optionNo),
        required = true
    )

    // ---------------- Caregiver counselling ----------------
    private val caregiverCounsellingProvided = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = context.getString(R.string.psychosocial_caregiver_counselling),
        entries = arrayOf(optionYes, optionNo),
        required = true
    )

    // ---------------- Caregiver distress ----------------
    private val caregiverDistressIdentified = FormElement(
        id = 3,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.psychosocial_caregiver_distress),
        entries = arrayOf(optionYes),
        required = false
    )

    // ---------------- Counselling remarks ----------------
    private val counsellingRemarks = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = context.getString(R.string.psychosocial_counselling_remarks),
        required = false,
        etMaxLength = 250,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT
    )

    // ---------------- Section F: Referral & Follow-up ----------------

    override val referralRequired = createReferralRequired(5)

    override val referralLevel = createReferralLevel(6)

    override val reasonForReferral = createReasonForReferral(7)

    override val followUpRequired = createFollowUpRequired(8)

    override val followUpDate = createFollowUpDate(9)
    override val caseStatus = createCaseStatus(20)
    override val dateOfDeath = createDateOfDeath(21)
    override val remarks = createRemarks(22)

    // Referral priority (Routine / Urgent / Emergency), shown on the referral = Yes path.
    private val referralPriority = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.psychosocial_referral_priority_title),
        entries = context.resources.getStringArray(R.array.mh_referral_priority_options),
        required = true
    )

    override fun additionalReferralYesFields(): List<FormElement> = listOf(referralPriority)

    // ---------------- Setup Page ----------------
    suspend fun setUpPage(savedRecord: PsychosocialCaregiverSupport?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)

        setUpPage(buildPageList())
    }

    private fun buildPageList(): List<FormElement> {
        val list = mutableListOf<FormElement>()
        list.add(psychosocialCounsellingProvided)
        list.add(caregiverCounsellingProvided)
        list.add(caregiverDistressIdentified)
        list.add(counsellingRemarks)

        // Section F
        addReferralFollowUpElements(list)

        return list
    }

    private suspend fun rebuildPage() {
        setUpPage(buildPageList())
    }

    // ---------------- Value Change Handler ----------------
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        // Referral answer toggles Case Status visibility → full rebuild instead of the
        // base's incremental dependant handling.
        if (formId == referralRequired.id) {
            if (isReferralYes()) {
                referralLevel.required = true
                caseStatus.value = null
                dateOfDeath.value = null
            } else {
                referralLevel.value = null
                referralLevel.required = false
                reasonForReferral.value = null
                referralPriority.value = null
            }
            rebuildPage()
            return referralRequired.id
        }
        return handleReferralFollowUpChange(formId, index)
    }

    // ---------------- Cache Helpers ----------------
    private fun createDefaultCache(): PsychosocialCaregiverSupport {
        return PsychosocialCaregiverSupport(
            patientId = "",
            benVisitNo = null
        )
    }

    private fun populateFromCache(cache: PsychosocialCaregiverSupport) {
        psychosocialCounsellingProvided.value =
            cache.psychosocialCounsellingProvided?.let { if (it) optionYes else optionNo }

        caregiverCounsellingProvided.value =
            cache.caregiverCounsellingProvided?.let { if (it) optionYes else optionNo }

        caregiverDistressIdentified.value =
            if (cache.caregiverDistressIdentified == true) optionYes else null

        counsellingRemarks.value = cache.counsellingRemarks

        // Section F
        populateReferralFollowUpFromCache(cache)
        // Dropdown stored English-canonical in DB; re-localize for display.
        referralPriority.value =
            getLocalValueInArray(R.array.mh_referral_priority_options, cache.referralPriority)
    }

    // ---------------- Map Values ----------------
    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PsychosocialCaregiverSupport).let {

            it.psychosocialCounsellingProvided =
                psychosocialCounsellingProvided.value == optionYes

            it.caregiverCounsellingProvided =
                caregiverCounsellingProvided.value == optionYes

            it.caregiverDistressIdentified =
                caregiverDistressIdentified.value == optionYes

            it.counsellingRemarks = counsellingRemarks.value

            // Section F
            mapReferralFollowUpValues(it)
            // Persist referral priority English-canonical, only on the referral = Yes path.
            it.referralPriority = if (isReferralYes()) {
                getEnglishValueInArray(R.array.mh_referral_priority_options, referralPriority.value)
            } else {
                null
            }
        }
    }
}