package org.piramalswasthya.cho.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.network.getLongFromDate
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private fun formatMotherFullName(patient: Patient): String {
    return "${patient.firstName} ${patient.lastName ?: ""}".trim()
}

private fun formatBabyOrdinalName(motherFirstName: String?, babyIndex: Int): String {
    return when (babyIndex) {
        0 -> "1st baby of $motherFirstName"
        1 -> "2nd baby of $motherFirstName"
        2 -> "3rd baby of $motherFirstName"
        else -> "${babyIndex + 1}th baby of $motherFirstName"
    }
}

@Entity(
    tableName = "INFANT_REG",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"),
        childColumns = arrayOf("motherPatientID"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "infRegInd", value = ["motherPatientID"])]
)
data class InfantRegCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var childPatientID: String? = null,
    val motherPatientID: String,
    var isActive: Boolean,
    var babyName: String? = null,
    var babyIndex: Int,
    var infantTerm: String? = null,
    var corticosteroidGiven: String? = null,
    var genderID: Int? = null,
    var babyCriedAtBirth: Boolean? = null,
    var resuscitation: Boolean? = null,
    var referred: String? = null,
    var hadBirthDefect: String? = null,
    var birthDefect: String? = null,
    var otherDefect: String? = null,
    var weight: Double? = null,
    var breastFeedingStarted: Boolean? = null,
    var opv0Dose: Long? = null,
    var bcgDose: Long? = null,
    var hepBDose: Long? = null,
    var vitkDose: Long? = null,

    // BRD Neonatal Outcome fields
    /** Q2: Live Birth / Still Birth (Macerated) / Still Birth (Fresh) / Died during delivery */
    var outcomeAtBirth: String? = null,
    /** Q5: Comma-separated multi-select resuscitation types */
    var typeOfResuscitation: String? = null,
    /** Q10: Comma-separated multi-select newborn complications */
    var newbornComplications: String? = null,
    /** Q11: Healthy and with mother / Admitted (SNCU/NICU) / Admitted (General ward) / Died */
    var currentStatusOfBaby: String? = null,
    /** Q12: Comma-separated multi-select cause of death */
    var causeOfDeath: String? = null,
    /** Q13: Other cause of death free text */
    var otherCauseOfDeath: String? = null,
    /** Q14: Comma-separated multi-select vaccines (BCG, Hepatitis B, OPV-0, None) */
    var birthDoseVaccinesGiven: String? = null,
    /** Q15: Reason for not giving vaccines */
    var reasonForNoVaccines: String? = null,
    /** Q16: Vitamin K injection given */
    var vitaminKInjectionGiven: Boolean? = null,
    /** Q17: Reason for not giving Vitamin K */
    var reasonForNoVitaminK: String? = null,
    /** Q18: Yes / In process / No (Not applied) */
    var birthCertificateIssued: String? = null,

    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    fun hasRegistrationData(): Boolean {
        return !infantTerm.isNullOrBlank() ||
            !corticosteroidGiven.isNullOrBlank() ||
            genderID != null ||
            babyCriedAtBirth != null ||
            resuscitation != null ||
            !referred.isNullOrBlank() ||
            !hadBirthDefect.isNullOrBlank() ||
            !birthDefect.isNullOrBlank() ||
            !otherDefect.isNullOrBlank() ||
            weight != null ||
            breastFeedingStarted != null ||
            opv0Dose != null ||
            bcgDose != null ||
            hepBDose != null ||
            vitkDose != null ||
            !outcomeAtBirth.isNullOrBlank() ||
            !typeOfResuscitation.isNullOrBlank() ||
            !newbornComplications.isNullOrBlank() ||
            !currentStatusOfBaby.isNullOrBlank() ||
            !causeOfDeath.isNullOrBlank() ||
            !otherCauseOfDeath.isNullOrBlank() ||
            !birthDoseVaccinesGiven.isNullOrBlank() ||
            !reasonForNoVaccines.isNullOrBlank() ||
            vitaminKInjectionGiven != null ||
            !reasonForNoVitaminK.isNullOrBlank() ||
            !birthCertificateIssued.isNullOrBlank()
    }

    private fun getDateStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        dateLong?.let {
            return dateFormat.format(dateLong)
        } ?: run {
            return null
        }
    }

    fun asPostModel(): InfantRegPost {
        return InfantRegPost(
            id = id,
            patientID = motherPatientID,
            childPatientID = childPatientID,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            genderID = genderID,
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = opv0Dose?.let { getDateStringFromLong(it) },
            bcgDose = bcgDose?.let { getDateStringFromLong(it) },
            hepBDose = hepBDose?.let { getDateStringFromLong(it) },
            vitkDose = vitkDose?.let { getDateStringFromLong(it) },
            outcomeAtBirth = outcomeAtBirth,
            typeOfResuscitation = typeOfResuscitation,
            newbornComplications = newbornComplications,
            currentStatusOfBaby = currentStatusOfBaby,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            birthDoseVaccinesGiven = birthDoseVaccinesGiven,
            reasonForNoVaccines = reasonForNoVaccines,
            vitaminKInjectionGiven = vitaminKInjectionGiven,
            reasonForNoVitaminK = reasonForNoVitaminK,
            birthCertificateIssued = birthCertificateIssued,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

/**
 * Patient with Delivery Outcome and Infant Registration data
 */
data class PatientWithDeliveryOutcomeAndInfantRegCache(
    @Embedded
    val patient: Patient,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val deliveryOutcome: List<DeliveryOutcomeCache>?,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "motherPatientID"
    )
    val savedInfantRegRecords: List<InfantRegCache>
) {
    fun asDomainModel(): List<InfantRegDomain> {
        val activeDo = deliveryOutcome?.firstOrNull { it.isActive } ?: return emptyList()
        val activeIr = savedInfantRegRecords.filter { it.isActive }
        val list = mutableListOf<InfantRegDomain>()
        val totalBirths = (activeDo.deliveryOutcome ?: ((activeDo.liveBirth ?: 0) + (activeDo.stillBirth ?: 0)))
            .coerceAtLeast(0)
        if (totalBirths == 0) return emptyList()
        val numLiveBirth = (activeDo.liveBirth ?: 0).coerceAtLeast(0)

        for (i in 0 until totalBirths) {
            list.add(
                InfantRegDomain(
                    motherPatient = patient,
                    babyIndex = i,
                    deliveryOutcome = activeDo,
                    savedIr = activeIr.firstOrNull { it.babyIndex == i },
                    isLiveBirth = i < numLiveBirth
                )
            )
        }
        return list
    }
}

/**
 * Domain model for displaying infant registration list
 */
data class InfantRegDomain(
    val motherPatient: Patient,
    val babyIndex: Int,
    var babyName: String = "Baby ${babyIndex + 1} of ${motherPatient.firstName}",
    val deliveryOutcome: DeliveryOutcomeCache,
    val savedIr: InfantRegCache?,
    val isLiveBirth: Boolean = true,
    val syncState: SyncState? = savedIr?.syncState
) {
    /**
     * Get formatted baby name (1st baby, 2nd baby, etc.)
     */
    val customName: String
        get() = savedIr?.babyName?.takeIf { it.isNotBlank() }
            ?: formatBabyOrdinalName(motherPatient.firstName, babyIndex)

    /**
     * Get mother's full name
     */
    fun getMotherFullName(): String {
        return formatMotherFullName(motherPatient)
    }

    /**
     * Get mother's age string for display (e.g. "29 YEARS" or "NA").
     */
    fun getMotherAgeString(): String {
        return motherPatient.dob?.let { DateTimeUtil.calculateAgeString(it) } ?: "NA"
    }

    /**
     * Check if infant is registered
     */
    fun isRegistered(): Boolean = savedIr?.hasRegistrationData() == true

    /**
     * Only live-birth rows should show Register when not yet saved.
     * Non-live-birth rows are always View state in list.
     */
    fun shouldShowRegisterAction(): Boolean = isLiveBirth && !isRegistered()

    /**
     * Guard clicks for non-live-birth rows with no saved record.
     */
    fun isActionEnabled(): Boolean = isLiveBirth || isRegistered()
}

data class InfantRegPost(
    val id: Long = 0,
    val patientID: String,
    val childPatientID: String?,
    val isActive: Boolean,
    val babyName: String? = null,
    val babyIndex: Int,
    val infantTerm: String? = null,
    val corticosteroidGiven: String? = null,
    val genderID: Int? = null,
    val babyCriedAtBirth: Boolean? = null,
    val resuscitation: Boolean? = null,
    val referred: String? = null,
    val hadBirthDefect: String? = null,
    val birthDefect: String? = null,
    val otherDefect: String? = null,
    val weight: Double? = null,
    val breastFeedingStarted: Boolean? = null,
    val opv0Dose: String? = null,
    val bcgDose: String? = null,
    val hepBDose: String? = null,
    val vitkDose: String? = null,
    val outcomeAtBirth: String? = null,
    val typeOfResuscitation: String? = null,
    val newbornComplications: String? = null,
    val currentStatusOfBaby: String? = null,
    val causeOfDeath: String? = null,
    val otherCauseOfDeath: String? = null,
    val birthDoseVaccinesGiven: String? = null,
    val reasonForNoVaccines: String? = null,
    val vitaminKInjectionGiven: Boolean? = null,
    val reasonForNoVitaminK: String? = null,
    val birthCertificateIssued: String? = null,
    val createdDate: String? = null,
    val createdBy: String,
    val updatedDate: String? = null,
    val updatedBy: String
) {
    fun toCacheModel(): InfantRegCache {
        return InfantRegCache(
            id = id,
            motherPatientID = patientID,
            childPatientID = childPatientID,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            genderID = genderID,
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = getLongFromDate(opv0Dose),
            bcgDose = getLongFromDate(bcgDose),
            hepBDose = getLongFromDate(hepBDose),
            vitkDose = getLongFromDate(vitkDose),
            outcomeAtBirth = outcomeAtBirth,
            typeOfResuscitation = typeOfResuscitation,
            newbornComplications = newbornComplications,
            currentStatusOfBaby = currentStatusOfBaby,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            birthDoseVaccinesGiven = birthDoseVaccinesGiven,
            reasonForNoVaccines = reasonForNoVaccines,
            vitaminKInjectionGiven = vitaminKInjectionGiven,
            reasonForNoVitaminK = reasonForNoVitaminK,
            birthCertificateIssued = birthCertificateIssued,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}

/**
 * FLW API payload for infant registration sync.
 * Uses beneficiary IDs (benId/childBenId) to align with server contract.
 */
data class InfantRegApiPost(
    val id: Long = 0,
    val benId: Long,
    val childBenId: Long,
    val isActive: Boolean,
    val babyName: String? = null,
    val babyIndex: Int,
    val infantTerm: String? = null,
    val corticosteroidGiven: String? = null,
    val gender: String? = null,
    val babyCriedAtBirth: Boolean? = null,
    val resuscitation: Boolean? = null,
    val referred: String? = null,
    val hadBirthDefect: String? = null,
    val birthDefect: String? = null,
    val otherDefect: String? = null,
    val weight: Double = 0.0,
    val breastFeedingStarted: Boolean? = null,
    val opv0Dose: String? = null,
    val bcgDose: String? = null,
    val hepBDose: String? = null,
    val vitkDose: String? = null,
    val deliveryDischargeSummary1: String? = null,
    val deliveryDischargeSummary2: String? = null,
    val deliveryDischargeSummary3: String? = null,
    val deliveryDischargeSummary4: String? = null,
    val isSNCU: String? = null,
    val outcomeAtBirth: String? = null,
    val typeOfResuscitation: String? = null,
    val newbornComplications: String? = null,
    val currentStatusOfBaby: String? = null,
    val causeOfDeath: String? = null,
    val otherCauseOfDeath: String? = null,
    val birthDoseVaccinesGiven: String? = null,
    val reasonForNoVaccines: String? = null,
    val vitaminKInjectionGiven: Boolean? = null,
    val reasonForNoVitaminK: String? = null,
    val birthCertificateIssued: String? = null,
    val createdDate: String? = null,
    val createdBy: String? = null,
    val updatedDate: String? = null,
    val updatedBy: String? = null
)

/**
 * HWC Child API payload (`/child/saveAll`, `/child/getAll`).
 */
data class ChildApiPost(
    val id: Long = 0,
    val benId: Long,
    val babyName: String? = null,
    val infantTerm: String? = null,
    val corticosteroidGiven: String? = null,
    val gender: String? = null,
    val babyCriedAtBirth: Boolean? = null,
    val resuscitation: Boolean? = null,
    val referred: String? = null,
    val hadBirthDefect: String? = null,
    val birthDefect: String? = null,
    val otherDefect: String? = null,
    val weight: Double = 0.0,
    val breastFeedingStarted: Boolean? = null,
    val opv0Dose: String? = null,
    val bcgDose: String? = null,
    val hepBDose: String? = null,
    val vitkDose: String? = null,
    val createdDate: String? = null,
    val createdBy: String? = null,
    val updatedDate: String? = null,
    val updatedBy: String? = null
)

fun getIsoDateTimeStringFromLong(dateLong: Long?): String? {
    if (dateLong == null) return null
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(dateLong)
}

/**
 * Infant Registration with Patient (mother and child) relation
 * Used for Child Registration list
 */
data class InfantRegWithPatient(
    @Embedded
    val infant: InfantRegCache,
    @Relation(
        parentColumn = "motherPatientID",
        entityColumn = "patientID"
    )
    val motherPatient: Patient,
    @Relation(
        parentColumn = "childPatientID",
        entityColumn = "patientID"
    )
    val childPatient: Patient?
) {
    fun asDomainModel(): ChildRegDomain {
        return ChildRegDomain(
            motherPatient = motherPatient,
            infant = infant,
            childPatient = childPatient
        )
    }
}

/**
 * Domain model for displaying child registration list
 */
data class ChildRegDomain(
    val motherPatient: Patient,
    val infant: InfantRegCache,
    val childPatient: Patient?,
    val syncState: SyncState? = infant.syncState,
    val displaySyncState: SyncState? = if (infant.processed == "C" || childPatient != null) infant.syncState else null
) {
    /**
     * Get formatted baby name (baby 0, baby 1, etc.)
     */
    val customName: String
        get() {
            val childFullName = childPatient?.let {
                "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim()
            }?.takeIf { it.isNotBlank() }

            return childFullName
                ?: infant.babyName?.takeIf { it.isNotBlank() }
                ?: formatBabyOrdinalName(motherPatient.firstName, infant.babyIndex)
        }

    /**
     * Get mother's full name
     */
    fun getMotherFullName(): String {
        return formatMotherFullName(motherPatient)
    }

    /**
     * Check if child patient is registered
     */
    fun isChildRegistered(): Boolean = infant.processed == "C" || childPatient != null
}
