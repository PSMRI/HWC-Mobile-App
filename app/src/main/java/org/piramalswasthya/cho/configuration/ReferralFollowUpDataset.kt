package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.ReferralFollowUpModel
import java.util.concurrent.TimeUnit

abstract class ReferralFollowUpDataset(context: Context, currentLanguage: Languages) : Dataset(context, currentLanguage) {

    protected abstract val referralRequired: FormElement
    protected abstract val referralLevel: FormElement
    protected abstract val reasonForReferral: FormElement
    protected abstract val followUpRequired: FormElement
    protected abstract val followUpDate: FormElement

    protected fun createReferralRequired(id: Int) = FormElement(
        id = id,
        inputType = InputType.RADIO,
        title = "Referral required",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    protected fun createReferralLevel(id: Int) = FormElement(
        id = id,
        inputType = InputType.DROPDOWN,
        title = "Referral level",
        entries = arrayOf("PHC", "CHC", "District Hospital", "Palliative Care Unit"),
        required = false
    )

    protected fun createReasonForReferral(id: Int) = FormElement(
        id = id,
        inputType = InputType.DROPDOWN,
        title = "Reason for referral",
        entries = arrayOf(
            "Severe pain",
            "Functional dependence",
            "Dementia suspected",
            "End-of-life care"
        ),
        required = true
    )

    protected fun createFollowUpRequired(id: Int) = FormElement(
        id = id,
        inputType = InputType.RADIO,
        title = "Follow-up required",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    protected fun createFollowUpDate(id: Int) = FormElement(
        id = id,
        inputType = InputType.DATE_PICKER,
        title = "Follow-up date",
        required = false
    )

    protected fun addReferralFollowUpElements(list: MutableList<FormElement>) {
        list.add(referralRequired)
        if (referralRequired.value == "Yes") {
            referralLevel.required = true
            list.add(referralLevel)
            list.add(reasonForReferral)
            list.add(followUpRequired)
            if (followUpRequired.value == "Yes") {
                followUpDate.required = true
                followUpDate.min = System.currentTimeMillis()
                followUpDate.max = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 10)
                list.add(followUpDate)
            }
        }
    }

    protected suspend fun handleReferralFollowUpChange(formId: Int, index: Int): Int {
        return when (formId) {
            referralRequired.id -> {
                if (index == 0) { // Yes
                    referralLevel.required = true
                    val addItems = mutableListOf(referralLevel, reasonForReferral, followUpRequired)
                    if (followUpRequired.value == "Yes") {
                        followUpDate.required = true
                        followUpDate.min = System.currentTimeMillis()
                        followUpDate.max = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 10)
                        addItems.add(followUpDate)
                    }
                    triggerDependants(
                        source = referralRequired,
                        addItems = addItems,
                        removeItems = emptyList()
                    )
                } else { // No
                    referralLevel.value = null
                    referralLevel.required = false
                    reasonForReferral.value = null
                    followUpRequired.value = null
                    followUpDate.value = null
                    followUpDate.required = false
                    triggerDependants(
                        source = referralRequired,
                        addItems = emptyList(),
                        removeItems = listOf(referralLevel, reasonForReferral, followUpRequired, followUpDate)
                    )
                }
                referralRequired.id
            }
            followUpRequired.id -> {
                if (index == 0) { // Yes
                    followUpDate.required = true
                    followUpDate.min = System.currentTimeMillis()
                    followUpDate.max = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 10)
                    triggerDependants(
                        source = followUpRequired,
                        addItems = listOf(followUpDate),
                        removeItems = emptyList()
                    )
                } else { // No
                    followUpDate.value = null
                    followUpDate.required = false
                    triggerDependants(
                        source = followUpRequired,
                        addItems = emptyList(),
                        removeItems = listOf(followUpDate)
                    )
                }
                followUpRequired.id
            }
            else -> -1
        }
    }

    protected fun populateReferralFollowUpFromCache(cacheValue: ReferralFollowUpModel) {
        referralRequired.value = if (cacheValue.referralRequired == true) "Yes" else if (cacheValue.referralRequired == false) "No" else null
        referralLevel.value = cacheValue.referralLevel
        reasonForReferral.value = cacheValue.reasonForReferral
        followUpRequired.value = if (cacheValue.followUpRequired == true) "Yes" else if (cacheValue.followUpRequired == false) "No" else null
        followUpDate.value = cacheValue.followUpDate
    }

    protected fun mapReferralFollowUpValues(cacheValue: ReferralFollowUpModel) {
        cacheValue.referralRequired = when (referralRequired.value) {
            "Yes" -> true
            "No" -> false
            else -> null
        }
        if (cacheValue.referralRequired == true) {
            cacheValue.referralLevel = referralLevel.value
            cacheValue.reasonForReferral = reasonForReferral.value
            cacheValue.followUpRequired = when (followUpRequired.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            if (cacheValue.followUpRequired == true) {
                cacheValue.followUpDate = followUpDate.value
            } else {
                cacheValue.followUpDate = null
            }
        } else {
            cacheValue.referralLevel = null
            cacheValue.reasonForReferral = null
            cacheValue.followUpRequired = null
            cacheValue.followUpDate = null
        }
    }
}
