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
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {

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
        val numLiveBirth = activeDo.liveBirth ?: 1
        if (numLiveBirth == 0) return emptyList()
        
        for (i in 0 until numLiveBirth) {
            list.add(
                InfantRegDomain(
                    motherPatient = patient,
                    babyIndex = i,
                    deliveryOutcome = activeDo,
                    savedIr = activeIr.firstOrNull { it.babyIndex == i },
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
    val syncState: SyncState? = savedIr?.syncState
) {
    /**
     * Get formatted baby name (1st baby, 2nd baby, etc.)
     */
    val customName: String
        get() = when (babyIndex) {
            0 -> "1st baby of ${motherPatient.firstName}"
            1 -> "2nd baby of ${motherPatient.firstName}"
            2 -> "3rd baby of ${motherPatient.firstName}"
            else -> "${babyIndex + 1}th baby of ${motherPatient.firstName}"
        }

    /**
     * Get mother's full name
     */
    fun getMotherFullName(): String {
        return "${motherPatient.firstName} ${motherPatient.lastName ?: ""}".trim()
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
    fun isRegistered(): Boolean = savedIr != null
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
    val childPatient: Patient?
) {
    /**
     * Get formatted baby name (baby 0, baby 1, etc.)
     */
    val customName: String
        get() = "baby ${infant.babyIndex} of ${motherPatient.firstName}"

    /**
     * Get mother's full name
     */
    fun getMotherFullName(): String {
        return "${motherPatient.firstName} ${motherPatient.lastName ?: ""}".trim()
    }

    /**
     * Check if child patient is registered
     */
    fun isChildRegistered(): Boolean = childPatient != null
}
