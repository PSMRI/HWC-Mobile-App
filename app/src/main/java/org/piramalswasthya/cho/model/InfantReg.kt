package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.network.getLongFromDate
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
    var gender: Gender? = null,
    var babyCriedAtBirth: Boolean? = null,
    var resuscitation: Boolean? = null,
    var referred: String? = null,
    var hadBirthDefect: String? = null,
    var birthDefect: String? = null,

    var isSNCU: String? = null,
    var deliveryDischargeSummary1 : String? = null,
    var deliveryDischargeSummary2 : String? = null,
    var deliveryDischargeSummary3 : String? = null,
    var deliveryDischargeSummary4 : String? = null,

    var otherDefect: String? = null,
    var weight: Double? = 0.0,
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

    fun asPostModel(benId: Long): InfantRegPost {
        return InfantRegPost(
            id = id,
            benId = benId,
            childBenId = childPatientID?.toLongOrNull() ?: 0,
            isSNCU = isSNCU,
            deliveryDischargeSummary1 = deliveryDischargeSummary1,
            deliveryDischargeSummary2 = deliveryDischargeSummary2,
            deliveryDischargeSummary3 = deliveryDischargeSummary3,
            deliveryDischargeSummary4 = deliveryDischargeSummary4,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            gender = gender?.name,
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

data class InfantRegPost(
    val id: Long = 0,
    val benId: Long,
    val childBenId: Long,
    val isActive: Boolean,
    var isSNCU: String? = null,
    var deliveryDischargeSummary1 : String? = null,
    var deliveryDischargeSummary2 : String? = null,
    var deliveryDischargeSummary3 : String? = null,
    var deliveryDischargeSummary4 : String? = null,

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
)

data class InfantRegDomain(
    val motherPatientID: String,
    val motherName: String,
    val babyIndex: Int,
    var babyName: String = "Baby ${babyIndex + 1} of $motherName",
    val deliveryOutcome: DeliveryOutcomeCache?,
    val savedIr: InfantRegCache?,
    val syncState: SyncState? = savedIr?.syncState
) {
    val customName: String
        get() = when (babyIndex) {
            0 -> "1st baby of $motherName"
            1 -> "2nd baby of $motherName"
            2 -> "3rd baby of $motherName"
            else -> "${babyIndex + 1}th baby of $motherName"
        }
}
