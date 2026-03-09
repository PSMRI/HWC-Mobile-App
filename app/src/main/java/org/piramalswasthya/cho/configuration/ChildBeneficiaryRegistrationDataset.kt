package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.network.getDateFromLong

class ChildBeneficiaryRegistrationDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val childName = FormElement(
        id = 1,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.name_of_child),
        required = true,
        hasDependants = false
    )

    private val dob = FormElement(
        id = 2,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date_of_birth),
        required = true,
        hasDependants = false,
        isEnabled = false,
        max = System.currentTimeMillis()
    )

    private val sex = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_sex),
        entries = resources.getStringArray(R.array.no_sex_array),
        required = true,
        hasDependants = false,
        isEnabled = false
    )

    private val motherName = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.mother_name),
        required = false,
        hasDependants = false
    )

    private val fatherName = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.father_s_name),
        required = false,
        hasDependants = false
    )

    private val mobileNumber = FormElement(
        id = 6,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.tv_mob_no_ph),
        required = false,
        hasDependants = false
    )

    private val birthWeight = FormElement(
        id = 7,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.no_birth_weight),
        required = false,
        hasDependants = false
    )

    suspend fun setUpPage(
        motherPatient: Patient,
        deliveryOutcomeCache: DeliveryOutcomeCache,
        infantRegCache: InfantRegCache,
        existingChild: Patient?
    ) {
        val fullMotherName =
            "${motherPatient.firstName.orEmpty()} ${motherPatient.lastName.orEmpty()}".trim()
        val childDisplayName = existingChild?.let { child ->
            listOfNotNull(child.firstName, child.lastName).joinToString(" ").trim()
        }.takeUnless { it.isNullOrBlank() }
            ?: infantRegCache.babyName
            ?: "baby of ${motherPatient.firstName.orEmpty()}".trim()

        childName.value = childDisplayName
        motherName.value = fullMotherName
        fatherName.value = motherPatient.spouseName ?: motherPatient.parentName ?: ""
        mobileNumber.value = motherPatient.phoneNo ?: ""
        birthWeight.value = infantRegCache.weight?.toString()

        val childDobMillis = existingChild?.dob?.time ?: deliveryOutcomeCache.dateOfDelivery
        dob.value = childDobMillis?.let { getDateFromLong(it) }
        dob.min = childDobMillis

        val sexId = existingChild?.genderID ?: infantRegCache.genderID
        sex.value = sexId?.let { id ->
            sex.entries?.getOrNull(id - 1)
        }

        setUpPage(
            listOf(
                childName,
                dob,
                sex,
                motherName,
                fatherName,
                mobileNumber,
                birthWeight
            )
        )
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            childName.id -> validateAllAlphabetsSpecialOnEditText(childName)
            else -> -1
        }
    }

    fun getChildFirstName(): String {
        val name = childName.value?.trim().orEmpty()
        return name.substringBefore(" ").ifBlank { name }
    }

    fun getChildLastName(): String? {
        val name = childName.value?.trim().orEmpty()
        val last = name.substringAfter(" ", "").trim()
        return last.ifBlank { null }
    }

    fun getChildFullName(): String = childName.value?.trim().orEmpty()

    fun getDobMillis(): Long? = Dataset.getLongFromDate(dob.value).takeIf { it != 0L }

    fun getGenderID(): Int? {
        val selected = sex.value ?: return null
        return sex.entries?.indexOf(selected)?.plus(1)
    }

    fun getFatherName(): String? = fatherName.value?.trim().takeUnless { it.isNullOrEmpty() }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        // This dataset maps directly to Patient entity fields via getters.
    }
}
