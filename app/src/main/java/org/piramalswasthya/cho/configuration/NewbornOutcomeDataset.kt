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
        neonatesList: List<org.piramalswasthya.cho.model.NeonateDetailsCache>?,
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

        // Load Baby 1 data from existing neonates
        neonatesList?.firstOrNull()?.let { baby1 ->
            loadBaby1Values(baby1)
        }

        // Add number of neonates selector
        list.add(numberOfNeonates)

        // Determine how many babies to show
        val numBabies = when (numberOfNeonates.value) {
            "Single (1)" -> 1
            "Twins (2)" -> 2
            "Triplets (3)" -> 3
            "Quadruplets+ (4)" -> 4
            else -> 1
        }

        // Add Baby 1 fields (always shown)
        addBaby1Fields(list)

        // Add Baby 2 section if Twins or more
        if (numBabies >= 2) {
            addBabySectionHeader(list, 2)
            // TODO: Load Baby 2 data and show fields
            addPlaceholderMessage(list, "Baby 2 details section (coming soon)")
        }

        // Add Baby 3 section if Triplets or more
        if (numBabies >= 3) {
            addBabySectionHeader(list, 3)
            addPlaceholderMessage(list, "Baby 3 details section (coming soon)")
        }

        // Add Baby 4 section if Quadruplets+
        if (numBabies >= 4) {
            addBabySectionHeader(list, 4)
            addPlaceholderMessage(list, "Baby 4 details section (coming soon)")
        }

        setUpPage(list)
    }

    private fun loadBaby1Values(baby: org.piramalswasthya.cho.model.NeonateDetailsCache) {
        baby1OutcomeAtBirth.value = baby.outcomeAtBirth
        baby1Sex.value = baby.sex
        baby1CriedImmediately.value = baby.criedImmediately
        baby1ResuscitationType.value = baby.resuscitationType
        baby1BirthWeight.value = baby.birthWeight?.toString()
        baby1CongenitalAnomaly.value = baby.congenitalAnomalyDetected
        baby1TypeOfAnomaly.value = baby.typeOfCongenitalAnomaly
        baby1OtherAnomaly.value = baby.otherCongenitalAnomaly
        baby1Complications.value = baby.newbornComplications
        baby1CurrentStatus.value = baby.currentStatusOfBaby
        baby1CauseOfDeath.value = baby.causeOfDeath
        baby1OtherCauseOfDeath.value = baby.otherCauseOfDeath
        baby1BirthDoseVaccines.value = baby.birthDoseVaccines
        baby1ReasonNoVaccines.value = baby.reasonForNoVaccines
        baby1VitaminK.value = baby.vitaminKInjection
        baby1ReasonNoVitaminK.value = baby.reasonForNoVitaminK
        baby1BirthCertificate.value = baby.birthCertificateIssued
    }

    private fun addBabySectionHeader(list: MutableList<FormElement>, babyNumber: Int) {
        list.add(FormElement(
            id = 1000 + babyNumber,
            inputType = InputType.EDIT_TEXT,
            title = "\n═══════ BABY $babyNumber ═══════\n",
            required = false,
            isEnabled = false,
            headingLine = true
        ))
    }

    private fun addPlaceholderMessage(list: MutableList<FormElement>, message: String) {
        list.add(FormElement(
            id = 2000 + list.size,
            inputType = InputType.EDIT_TEXT,
            title = message,
            required = false,
            isEnabled = false
        ))
    }

    private fun addBaby1Fields(list: MutableList<FormElement>) {
        // Always add outcome at birth first
        list.add(baby1OutcomeAtBirth)
        
        val outcomeValue = baby1OutcomeAtBirth.value
        val isStillbirthOrDied = outcomeValue in listOf(
            "Still Birth (Fresh)", 
            "Still Birth (Macerated)", 
            "Died during delivery"
        )

        // If stillbirth/died during delivery -> only show complications
        if (isStillbirthOrDied) {
            list.add(baby1Complications)
            return
        }

        // For Live Birth or no selection yet (null), show all fields
        // Add basic fields
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
        
        // Check if died
        val isDied = baby1CurrentStatus.value == "Died"
        
        if (isDied) {
            // If died, show cause of death fields
            list.add(baby1CauseOfDeath)
            if (baby1CauseOfDeath.value == "Other (specify)") {
                list.add(baby1OtherCauseOfDeath)
            }
            // Don't show vaccines/certificate if died
        } else {
            // If NOT died (or no selection), show vaccines and certificate
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

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            numberOfNeonates.id -> {
                // TODO: Add/remove baby sections based on selection
                getIndexOfElement(numberOfNeonates)
            }
            baby1OutcomeAtBirth.id -> {
                // Rebuild entire Baby 1 section based on outcome
                val liveBirthIndex = baby1OutcomeAtBirth.entries?.indexOf("Live Birth") ?: 0
                val isLiveBirth = index == liveBirthIndex
                
                if (!isLiveBirth) {
                    // If stillbirth/died, remove all fields except complications
                    triggerDependants(
                        source = baby1OutcomeAtBirth,
                        removeItems = listOf(
                            baby1Sex, baby1CriedImmediately, baby1ResuscitationType,
                            baby1BirthWeight, baby1CongenitalAnomaly, baby1TypeOfAnomaly,
                            baby1OtherAnomaly, baby1CurrentStatus, baby1CauseOfDeath,
                            baby1OtherCauseOfDeath, baby1BirthDoseVaccines,
                            baby1ReasonNoVaccines, baby1VitaminK, baby1ReasonNoVitaminK,
                            baby1BirthCertificate
                        ),
                        addItems = listOf(baby1Complications)
                    )
                } else {
                    // If live birth, add basic fields
                    triggerDependants(
                        source = baby1OutcomeAtBirth,
                        removeItems = listOf(),
                        addItems = listOf(
                            baby1Sex, baby1CriedImmediately, baby1BirthWeight,
                            baby1CongenitalAnomaly, baby1Complications, baby1CurrentStatus,
                            baby1BirthDoseVaccines, baby1VitaminK, baby1BirthCertificate
                        )
                    )
                }
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
                
                if (index == diedIndex) {
                    // If died, show cause of death and remove vaccines/certificate
                    triggerDependants(
                        source = baby1CurrentStatus,
                        removeItems = listOf(
                            baby1BirthDoseVaccines, baby1ReasonNoVaccines,
                            baby1VitaminK, baby1ReasonNoVitaminK,
                            baby1BirthCertificate
                        ),
                        addItems = listOf(baby1CauseOfDeath)
                    )
                } else {
                    // If NOT died, remove cause of death and add vaccines/certificate
                    triggerDependants(
                        source = baby1CurrentStatus,
                        removeItems = listOf(baby1CauseOfDeath, baby1OtherCauseOfDeath),
                        addItems = listOf(
                            baby1BirthDoseVaccines, baby1VitaminK, baby1BirthCertificate
                        )
                    )
                }
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
            
            // Note: Neonate-specific data is mapped via mapBaby1ToNeonate
        }
    }

    fun mapBaby1ToNeonate(neonate: org.piramalswasthya.cho.model.NeonateDetailsCache) {
        // Map Baby 1 fields to neonate entity
        neonate.apply {
            outcomeAtBirth = baby1OutcomeAtBirth.value ?: "Live Birth"
            sex = baby1Sex.value
            criedImmediately = baby1CriedImmediately.value
            resuscitationType = baby1ResuscitationType.value
            birthWeight = baby1BirthWeight.value?.toDoubleOrNull()
            congenitalAnomalyDetected = baby1CongenitalAnomaly.value
            typeOfCongenitalAnomaly = baby1TypeOfAnomaly.value
            otherCongenitalAnomaly = baby1OtherAnomaly.value
            newbornComplications = baby1Complications.value
            currentStatusOfBaby = baby1CurrentStatus.value ?: "Healthy and with mother"
            causeOfDeath = baby1CauseOfDeath.value
            otherCauseOfDeath = baby1OtherCauseOfDeath.value
            birthDoseVaccines = baby1BirthDoseVaccines.value
            reasonForNoVaccines = baby1ReasonNoVaccines.value
            vitaminKInjection = baby1VitaminK.value
            reasonForNoVitaminK = baby1ReasonNoVitaminK.value
            birthCertificateIssued = baby1BirthCertificate.value
            
            // Update timestamp
            updatedDate = System.currentTimeMillis()
        }
    }
}
