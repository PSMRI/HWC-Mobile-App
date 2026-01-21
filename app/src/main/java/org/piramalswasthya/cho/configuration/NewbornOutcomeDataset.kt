package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.NewbornOutcomeCache

/**
 * Comprehensive Newborn Outcome Dataset
 * Implements all 18+ fields per neonate as per ticket requirements
 */
class NewbornOutcomeDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    // ========== SECTION 1: NUMBER OF NEONATES ==========
    
    private val numberOfNeonates = FormElement(
        id = 1,
        inputType = InputType.DROPDOWN,
        title = "Number of Neonates *",
        entries = arrayOf("Single (1)", "Twins (2)", "Triplets (3)", "Quadruplets+ (4)"),
        required = true,
        hasDependants = true
    )

    // ========== BABY 1 FIELDS ==========
    
    private val baby1OutcomeAtBirth = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = "Baby 1 - Outcome at Birth *",
        entries = arrayOf(
            "Live Birth",
            "Still Birth (Fresh)",
            "Still Birth (Macerated)",
            "Died during delivery"
        ),
        required = true,
        hasDependants = true
    )

    private val baby1Sex = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = "Baby 1 - Sex *",
        entries = arrayOf("Male", "Female", "Ambiguous"),
        required = true,
        hasDependants = true
    )

    private val baby1CriedImmediately = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = "Baby 1 - Cried Immediately After Birth? *",
        entries = arrayOf("Immediate cry", "Cried after resuscitation"),
        required = true,
        hasDependants = true
    )

    private val baby1ResuscitationType = FormElement(
        id = 13,
        inputType = InputType.DROPDOWN,
        title = "Baby 1 - Type of Resuscitation *",
        entries = arrayOf(
            "Stimulation",
            "Suctioning",
            "Bag and mask ventilation",
            "Oxygen",
            "Intubation",
            "Chest compressions",
            "Medications"
        ),
        required = true,
        hasDependants = false
    )

    private val baby1BirthWeight = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = "Baby 1 - Birth Weight (grams) *",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        required = true,
        min = 500,
        max = 6000,
        hasDependants = true
    )

    private val baby1CongenitalAnomaly = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = "Baby 1 - Any Congenital Anomaly Detected?",
        entries = arrayOf("Yes", "No", "Suspected (under evaluation)"),
        required = false,
        hasDependants = true
    )

    private val baby1TypeOfAnomaly = FormElement(
        id = 16,
        inputType = InputType.DROPDOWN,
        title = "Baby 1 - Type of Congenital Anomaly *",
        entries = arrayOf(
            "Neural tube defect",
            "Cleft lip/palate",
            "Club foot",
            "Down syndrome (suspected)",
            "Congenital heart defect (suspected)",
            "Limb defects",
            "Abdominal wall defect",
            "Genital abnormality",
            "Other"
        ),
        required = true,
        hasDependants = true
    )

    private val baby1OtherAnomaly = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = "Baby 1 - Other Congenital Anomaly (Specify) *",
        etMaxLength = 300,
        required = true,
        hasDependants = false
    )

    private val baby1Complications = FormElement(
        id = 18,
        inputType = InputType.DROPDOWN,
        title = "Baby 1 - Newborn Complications",
        entries = arrayOf(
            "Birth asphyxia",
            "Respiratory distress",
            "Neonatal jaundice",
            "Sepsis (suspected)",
            "Hypothermia",
            "Hypoglycemia",
            "Bleeding",
            "Convulsions",
            "None"
        ),
        required = false,
        hasDependants = true
    )

    private val baby1CurrentStatus = FormElement(
        id = 19,
        inputType = InputType.RADIO,
        title = "Baby 1 - Current Status of Baby *",
        entries = arrayOf(
            "Healthy and with mother",
            "Admitted (SNCU/NICU)",
            "Admitted (General ward)",
            "Died"
        ),
        required = true,
        hasDependants = true
    )

    private val baby1CauseOfDeath = FormElement(
        id = 20,
        inputType = InputType.DROPDOWN,
        title = "Baby 1 - If Baby Died, Cause of Death *",
        entries = arrayOf(
            "Birth asphyxia",
            "Prematurity",
            "Low birth weight complications",
            "Sepsis",
            "Congenital anomaly",
            "Respiratory distress",
            "Unknown",
            "Other (specify)"
        ),
        required = true,
        hasDependants = true
    )

    private val baby1OtherCauseOfDeath = FormElement(
        id = 21,
        inputType = InputType.EDIT_TEXT,
        title = "Baby 1 - Other Cause of Death (Specify) *",
        etMaxLength = 300,
        required = true,
        hasDependants = false
    )

    private val baby1BirthDoseVaccines = FormElement(
        id = 22,
        inputType = InputType.DROPDOWN,
        title = "Baby 1 - Birth Dose Vaccines Given? *",
        entries = arrayOf("BCG", "Hepatitis B (Birth dose)", "OPV-0", "None"),
        required = true,
        hasDependants = true
    )

    private val baby1ReasonNoVaccines = FormElement(
        id = 23,
        inputType = InputType.EDIT_TEXT,
        title = "Baby 1 - Reason for Not Giving Birth Dose Vaccines *",
        etMaxLength = 200,
        required = true,
        hasDependants = false
    )

    private val baby1VitaminK = FormElement(
        id = 24,
        inputType = InputType.RADIO,
        title = "Baby 1 - Vitamin K Injection Given? *",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val baby1ReasonNoVitaminK = FormElement(
        id = 25,
        inputType = InputType.EDIT_TEXT,
        title = "Baby 1 - Reason for Not Giving Vitamin K Injection *",
        etMaxLength = 200,
        required = true,
        hasDependants = false
    )

    private val baby1BirthCertificate = FormElement(
        id = 26,
        inputType = InputType.RADIO,
        title = "Baby 1 - Birth Certificate Issued? *",
        entries = arrayOf("Yes", "In process", "No (Not applied)"),
        required = true,
        hasDependants = false
    )

    suspend fun setUpPage(
        existingData: NewbornOutcomeCache?,
        patientID: String
    ) {
        val list = mutableListOf<FormElement>()

        // Set existing values if available
        existingData?.let { cache ->
            cache.numberOfNeonates?.let {
                numberOfNeonates.value = when (it) {
                    1 -> "Single (1)"
                    2 -> "Twins (2)"
                    3 -> "Triplets (3)"
                    else -> "Quadruplets+ (4)"
                }
            }
        }

        // Add number of neonates selector
        list.add(numberOfNeonates)

        // Add Baby 1 fields
        list.add(baby1OutcomeAtBirth)
        
        // Only show remaining fields if Live Birth
        if (baby1OutcomeAtBirth.value == "Live Birth" || baby1OutcomeAtBirth.value == null) {
            list.add(baby1Sex)
            list.add(baby1CriedImmediately)
            
            // Show resuscitation type only if "Cried after resuscitation"
            if (baby1CriedImmediately.value == "Cried after resuscitation") {
                list.add(baby1ResuscitationType)
            }
            
            list.add(baby1BirthWeight)
            list.add(baby1CongenitalAnomaly)
            
            // Show anomaly type if Yes/Suspected
            if (baby1CongenitalAnomaly.value in listOf("Yes", "Suspected (under evaluation)")) {
                list.add(baby1TypeOfAnomaly)
                if (baby1TypeOfAnomaly.value == "Other") {
                    list.add(baby1OtherAnomaly)
                }
            }
            
            list.add(baby1Complications)
            list.add(baby1CurrentStatus)
            
            // Show cause of death if Died
            if (baby1CurrentStatus.value == "Died") {
                list.add(baby1CauseOfDeath)
                if (baby1CauseOfDeath.value == "Other (specify)") {
                    list.add(baby1OtherCauseOfDeath)
                }
            }
            
            // Only show vaccines if not died
            if (baby1CurrentStatus.value != "Died") {
                list.add(baby1BirthDoseVaccines)
                if (baby1BirthDoseVaccines.value == "None") {
                    list.add(baby1ReasonNoVaccines)
                }
                
                list.add(baby1VitaminK)
                if (baby1VitaminK.value == "No") {
                    list.add(baby1ReasonNoVitaminK)
                }
                
                list.add(baby1BirthCertificate)
            }
        }

        // TODO: Add Baby 2, 3, 4 sections based on numberOfNeonates

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            numberOfNeonates.id -> {
                val triggerIndex = getIndexOfElement(numberOfNeonates)
                // TODO: Add/remove baby sections based on selection
                triggerIndex
            }
            baby1OutcomeAtBirth.id -> {
                val triggerIndex = getIndexOfElement(baby1OutcomeAtBirth)
                // TODO: Show/hide fields based on outcome
                triggerIndex
            }
            baby1CriedImmediately.id -> {
                val criedIndex = baby1CriedImmediately.entries?.indexOf("Cried after resuscitation") ?: 1
                triggerDependants(
                    source = baby1CriedImmediately,
                    passedIndex = index,
                    triggerIndex = criedIndex,
                    target = baby1ResuscitationType
                )
            }
            baby1CongenitalAnomaly.id -> {
                when (index) {
                    0, 2 -> { // "Yes" or "Suspected"
                        triggerDependants(
                            source = baby1CongenitalAnomaly,
                            removeItems = listOf(),
                            addItems = listOf(baby1TypeOfAnomaly)
                        )
                    }
                    else -> { // "No"
                        triggerDependants(
                            source = baby1CongenitalAnomaly,
                            removeItems = listOf(baby1TypeOfAnomaly, baby1OtherAnomaly),
                            addItems = listOf()
                        )
                    }
                }
            }
            baby1TypeOfAnomaly.id -> {
                val otherIndex = baby1TypeOfAnomaly.entries?.indexOf("Other") ?: 8
                triggerDependants(
                    source = baby1TypeOfAnomaly,
                    passedIndex = index,
                    triggerIndex = otherIndex,
                    target = baby1OtherAnomaly
                )
            }
            baby1CurrentStatus.id -> {
                val diedIndex = baby1CurrentStatus.entries?.indexOf("Died") ?: 3
                triggerDependants(
                    source = baby1CurrentStatus,
                    passedIndex = index,
                    triggerIndex = diedIndex,
                    target = baby1CauseOfDeath
                )
            }
            baby1CauseOfDeath.id -> {
                val otherIndex = baby1CauseOfDeath.entries?.indexOf("Other (specify)") ?: 7
                triggerDependants(
                    source = baby1CauseOfDeath,
                    passedIndex = index,
                    triggerIndex = otherIndex,
                    target = baby1OtherCauseOfDeath
                )
            }
            baby1BirthDoseVaccines.id -> {
                val noneIndex = baby1BirthDoseVaccines.entries?.indexOf("None") ?: 3
                triggerDependants(
                    source = baby1BirthDoseVaccines,
                    passedIndex = index,
                    triggerIndex = noneIndex,
                    target = baby1ReasonNoVaccines
                )
            }
            baby1VitaminK.id -> {
                val noIndex = baby1VitaminK.entries?.indexOf("No") ?: 1
                triggerDependants(
                    source = baby1VitaminK,
                    passedIndex = index,
                    triggerIndex = noIndex,
                    target = baby1ReasonNoVitaminK
                )
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as NewbornOutcomeCache).let { cache ->
            // Map mother-level data
            cache.numberOfNeonates = when (numberOfNeonates.value) {
                "Single (1)" -> 1
                "Twins (2)" -> 2
                "Triplets (3)" -> 3
                "Quadruplets+ (4)" -> 4
                else -> 1
            }
            
            // Note: Neonate-specific data is mapped in ViewModel
            // because we need to handle multiple NeonateDetailsCache objects
        }
    }
}
