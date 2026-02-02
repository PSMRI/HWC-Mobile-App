package org.piramalswasthya.cho.configuration

import android.content.Context
import android.widget.LinearLayout
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import java.util.concurrent.TimeUnit

/**
 * Dataset for recording mother's condition, complications, admission status post-delivery.
 * Aligns with FLW app behaviour and ticket: maternal condition, complications (multi-select),
 * mother currently admitted, date of discharge; triggers PNC actions and alerts.
 */
class DeliveryOutcomeDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var deliveryDateMillis: Long = 0L

    private val deliveryDate = FormElement(
        id = 0,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.delivery_date),
        arrayId = -1,
        required = true,
        hasDependants = false,
        max = System.currentTimeMillis()
    )

    /** 4 options: use vertical layout per requirement (vertical when >2 options). */
    private val motherCondition = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_mother_condition),
        entries = resources.getStringArray(R.array.do_mother_condition_array),
        required = true,
        hasDependants = true,
        hasAlertError = true,
        orientation = LinearLayout.VERTICAL
    )

    private val maternalComplications = FormElement(
        id = 2,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.do_maternal_complications),
        entries = resources.getStringArray(R.array.do_maternal_complications_array),
        required = true,
        hasDependants = true,
        hasAlertError = true
    )

    /** 2 options: use horizontal layout per requirement (horizontal when ≤2 options). */
    private val motherCurrentlyAdmitted = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_mother_currently_admitted),
        entries = resources.getStringArray(R.array.do_mother_admitted_array),
        required = true,
        hasDependants = true,
        orientation = LinearLayout.HORIZONTAL
    )

    private val dateOfDischarge = FormElement(
        id = 4,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_discharge_date),
        arrayId = -1,
        required = true,
        hasDependants = false,
        max = System.currentTimeMillis()
    )

    suspend fun setUpPage(deliveryDateMillis: Long, saved: DeliveryOutcomeCache?) {
        this.deliveryDateMillis = deliveryDateMillis
        dateOfDischarge.min = deliveryDateMillis
        dateOfDischarge.max = System.currentTimeMillis()
        deliveryDate.max = System.currentTimeMillis()

        val list = mutableListOf<FormElement>(
            deliveryDate,
            motherCondition,
            motherCurrentlyAdmitted
        )

        if (saved != null) {
            deliveryDate.value = saved.dateOfDelivery?.let { getDateFromLong(it) }
            this.deliveryDateMillis = saved.dateOfDelivery ?: deliveryDateMillis
            deliveryDate.min = this.deliveryDateMillis
            dateOfDischarge.min = this.deliveryDateMillis
            
            motherCondition.value = saved.motherCondition
            motherCurrentlyAdmitted.value = when (saved.motherCurrentlyAdmitted) {
                true -> resources.getStringArray(R.array.do_mother_admitted_array)[0]
                false -> resources.getStringArray(R.array.do_mother_admitted_array)[1]
                null -> null
            }
            maternalComplications.value = saved.maternalComplications
            dateOfDischarge.value = saved.dateOfDischarge?.let { getDateFromLong(it) }

            val conditionIndex = resources.getStringArray(R.array.do_mother_condition_array).indexOf(motherCondition.value)
            if (conditionIndex == 1 || conditionIndex == 2) {
                list.add(list.indexOf(motherCondition) + 1, maternalComplications)
            }
            if (motherCurrentlyAdmitted.value == resources.getStringArray(R.array.do_mother_admitted_array)[1]) {
                list.add(list.indexOf(motherCurrentlyAdmitted) + 1, dateOfDischarge)
            }
        } else {
            deliveryDate.value = getDateFromLong(deliveryDateMillis)
            motherCondition.value = null
            motherCurrentlyAdmitted.value = null
            maternalComplications.value = null
            dateOfDischarge.value = null
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            motherCondition.id -> {
                maternalComplications.value = null
                maternalComplications.errorText = null
                when (index) {
                    0, 3 -> triggerDependants(
                        source = motherCondition,
                        removeItems = listOf(maternalComplications),
                        addItems = emptyList()
                    )
                    1, 2 -> triggerDependants(
                        source = motherCondition,
                        removeItems = emptyList(),
                        addItems = listOf(maternalComplications)
                    )
                    else -> -1
                }
            }

            motherCurrentlyAdmitted.id -> {
                dateOfDischarge.value = null
                dateOfDischarge.errorText = null
                triggerDependants(
                    source = motherCurrentlyAdmitted,
                    passedIndex = index,
                    triggerIndex = 1,
                    target = dateOfDischarge
                )
            }

            deliveryDate.id -> {
                deliveryDate.value?.let { dateStr ->
                    val selectedDeliveryLong = getLongFromDate(dateStr)
                    if (selectedDeliveryLong != 0L) {
                        this.deliveryDateMillis = selectedDeliveryLong
                        dateOfDischarge.min = selectedDeliveryLong
                        // Re-validate discharge date: if set and now before new delivery date, show error
                        dateOfDischarge.value?.let { dischargeStr ->
                            val dischargeLong = getLongFromDate(dischargeStr)
                            dateOfDischarge.errorText = if (dischargeLong != 0L && dischargeLong < this.deliveryDateMillis) {
                                resources.getString(R.string.do_discharge_date_before_delivery)
                            } else null
                        }
                    }
                }
                -1
            }

            dateOfDischarge.id -> {
                dateOfDischarge.value?.let { dateStr ->
                    val dischargeLong = getLongFromDate(dateStr)
                    dateOfDischarge.errorText = if (dischargeLong != 0L && dischargeLong < deliveryDateMillis) {
                        resources.getString(R.string.do_discharge_date_before_delivery)
                    } else null
                } ?: run { dateOfDischarge.errorText = null }
                -1
            }

            maternalComplications.id -> {
                if (!maternalComplications.value.isNullOrBlank()) {
                    maternalComplications.errorText = null
                }
                -1
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        val form = cacheModel as DeliveryOutcomeCache
        val admittedYes = resources.getStringArray(R.array.do_mother_admitted_array)[0]
        val conditionMaternalDeath = resources.getStringArray(R.array.do_mother_condition_array)[3]
        form.dateOfDelivery = this.deliveryDate.value?.let { getLongFromDate(it) }.takeIf { it != 0L } ?: deliveryDateMillis
        form.motherCondition = this.motherCondition.value
        form.maternalComplications = this.maternalComplications.value?.takeIf { s -> s.isNotBlank() }
        form.motherCurrentlyAdmitted = this.motherCurrentlyAdmitted.value == admittedYes
        form.dateOfDischarge = this.dateOfDischarge.value?.let { getLongFromDate(it) }.takeIf { it != 0L }
        form.isDeath = this.motherCondition.value == conditionMaternalDeath
        if (form.isDeath == true) {
            form.isDeathValue = "Maternal Death"
        }
    }

    /** Index of Maternal Complications element (for showing intensive PNC / hysterectomy alerts). */
    fun getIndexOfMaternalComplications() = getIndexById(maternalComplications.id)

    /** True if Mother's Condition is Complication (specify) or Critical/ICU. */
    fun isComplicationOrCritical(): Boolean {
        val v = motherCondition.value ?: return false
        val arr = resources.getStringArray(R.array.do_mother_condition_array)
        return v == arr.getOrNull(1) || v == arr.getOrNull(2)
    }

    /** True if Mother's Condition is Maternal Death. */
    fun isMaternalDeath(): Boolean {
        val v = motherCondition.value ?: return false
        return v == resources.getStringArray(R.array.do_mother_condition_array).getOrNull(3)
    }

    /** True if selected complications include PPH or Uterine rupture (intensive PNC alert). */
    fun hasPphOrUterineRupture(): Boolean {
        val raw = maternalComplications.value ?: return false
        val pph = resources.getStringArray(R.array.do_maternal_complications_array).getOrNull(0) ?: "Post-Partum Hemorrhage (PPH)"
        val uterine = resources.getStringArray(R.array.do_maternal_complications_array).getOrNull(4) ?: "Uterine rupture"
        return raw.contains(pph) || raw.contains(uterine)
    }

    /** True if Hysterectomy performed is selected (family planning / EC update). */
    fun hasHysterectomy(): Boolean {
        val raw = maternalComplications.value ?: return false
        val hyst = resources.getStringArray(R.array.do_maternal_complications_array).getOrNull(8) ?: "Hysterectomy performed"
        return raw.contains(hyst)
    }
}
