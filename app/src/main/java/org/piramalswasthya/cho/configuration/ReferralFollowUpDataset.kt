package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.ReferralFollowUpModel
import java.util.concurrent.TimeUnit
import org.piramalswasthya.cho.R


abstract class ReferralFollowUpDataset(private val context: Context, currentLanguage: Languages) : Dataset(context, currentLanguage) {

    private val optionYes = context.getString(R.string.yes)
    private val optionNo = context.getString(R.string.no)

    protected abstract val referralRequired: FormElement
    protected abstract val referralLevel: FormElement
    protected abstract val reasonForReferral: FormElement
    protected abstract val followUpRequired: FormElement
    protected abstract val followUpDate: FormElement

    protected fun createReferralRequired(id: Int) = FormElement(
        id = id,
        inputType = InputType.RADIO,
        title = context.getString(R.string.referral_required_title),
        entries = context.resources.getStringArray(R.array.referral_required_options),
        trueIndex = 0,
        falseIndex = 1,
        required = true,
        hasDependants = true
    )

    protected fun createReferralLevel(id: Int) = FormElement(
        id = id,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.referral_level_title),
        entries = context.resources.getStringArray(R.array.referral_level_options),
        required = false
    )

    protected fun createReasonForReferral(id: Int) = FormElement(
        id = id,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.reason_for_referral_title),
        entries = context.resources.getStringArray(R.array.reason_for_referral_options),
        required = true
    )

    protected fun createFollowUpRequired(id: Int) = FormElement(
        id = id,
        inputType = InputType.RADIO,
        title = context.getString(R.string.follow_up_required_title),
        entries = context.resources.getStringArray(R.array.follow_up_required_options),
        trueIndex = 0,
        falseIndex = 1,
        required = true,
        hasDependants = true
    )

    protected fun createFollowUpDate(id: Int) = FormElement(
        id = id,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.follow_up_date_title),
        required = false
    )

    protected fun addReferralFollowUpElements(list: MutableList<FormElement>) {
        list.add(referralRequired)
        if (referralRequired.value == optionYes) {
            referralLevel.required = true
            list.add(referralLevel)
            list.add(reasonForReferral)
            list.add(followUpRequired)
            if (followUpRequired.value == optionYes) {
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
                if (index == referralRequired.trueIndex) {
                    referralLevel.required = true
                    val addItems = mutableListOf(referralLevel, reasonForReferral, followUpRequired)
                    if (followUpRequired.value == optionYes) {
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
                } else {
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
                if (index == followUpRequired.trueIndex) {
                    followUpDate.required = true
                    followUpDate.min = System.currentTimeMillis()
                    followUpDate.max = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 10)
                    triggerDependants(
                        source = followUpRequired,
                        addItems = listOf(followUpDate),
                        removeItems = emptyList()
                    )
                } else {
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
        referralRequired.value = when (cacheValue.referralRequired) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        referralLevel.value = cacheValue.referralLevel
        reasonForReferral.value = cacheValue.reasonForReferral
        followUpRequired.value = when (cacheValue.followUpRequired) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        followUpDate.value = cacheValue.followUpDate
    }

    protected fun mapReferralFollowUpValues(cacheValue: ReferralFollowUpModel) {
        cacheValue.referralRequired = when (referralRequired.value) {
            optionYes -> true
            optionNo -> false
            else -> null
        }
        if (cacheValue.referralRequired == true) {
            cacheValue.referralLevel = referralLevel.value
            cacheValue.reasonForReferral = reasonForReferral.value
            cacheValue.followUpRequired = when (followUpRequired.value) {
                optionYes -> true
                optionNo -> false
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
