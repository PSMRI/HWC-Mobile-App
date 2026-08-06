package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.utils.DateTimeUtil.Companion.getDateTimeStringFromLong
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "ELIGIBLE_COUPLE_TRACKING",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"/* "householdId"*/),
        childColumns = arrayOf("patientID" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_ect", value = ["patientID"/* "hhId"*/])]
)

data class EligibleCoupleTrackingCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientID: String,
    var visitDate: Long = System.currentTimeMillis(),
    var financialYear: String? = null,
    var visitMonth: String? = null,
    var lmpDate: Long? = null,
    var isPregnancyTestDone: String? = null,
    var pregnancyTestResult: String? = null,
    var isPregnant: String? = null,
    var usingFamilyPlanning: Boolean? = null,
    var methodOfContraception: String? = null,
    var anyOtherMethod: String? = null,
    // ANTRA Injection fields
    var antraDose: String? = null,
    var antraInjectionDate: Long? = null,
    var antraDueDate: Long? = null,
    // Sterilization fields
    var dateOfSterilization: Long? = null,
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var processed: String? = "N",
    var isActive: Boolean = true,
    var syncState: SyncState
) : FormDataModel {

    companion object {
        fun getECTFilledDateFromLong(long: Long): String {
            // Create fresh each call so Locale.getDefault() reflects the current app language
            val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
            return "Visited on ${dateFormat.format(long)}"
        }
    }

    fun getAntraDueDateString(): String {
        // Create fresh each call so Locale.getDefault() reflects the current app language
        val dueDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return antraInjectionDate?.let { injectionDate ->
            if (injectionDate > 0L) {
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = injectionDate

                cal.add(java.util.Calendar.DAY_OF_YEAR, 76)
                val startDate = dueDateFormat.format(cal.timeInMillis)

                cal.timeInMillis = injectionDate
                cal.add(java.util.Calendar.DAY_OF_YEAR, 120)
                val endDate = dueDateFormat.format(cal.timeInMillis)

                "$startDate to $endDate"
            } else "NA"
        } ?: antraDueDate?.let { dueDate ->
            if (dueDate > 0L) dueDateFormat.format(dueDate) else "NA"
        } ?: "NA"
    }

    fun asNetworkModel(benId: Long): ECTNetwork {
        return ECTNetwork(
            id = 0,
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate)!!,
            financialYear = financialYear,
            visitMonth = visitMonth,
            lmpDate = lmpDate?.let { getDateTimeStringFromLong(it) },
            isPregnancyTestDone = isPregnancyTestDone,
            pregnancyTestResult = pregnancyTestResult,
            isPregnant = isPregnant,
            usingFamilyPlanning = usingFamilyPlanning,
            methodOfContraception = methodOfContraception,
            anyOtherMethod = anyOtherMethod,
            antraDose = antraDose,
            dateOfAntraInjection = antraInjectionDate?.let { getDateTimeStringFromLong(it) },
            dueDateOfAntraInjection = antraDueDate?.let { getDateTimeStringFromLong(it) },
            dateOfSterilization = dateOfSterilization?.let { getDateTimeStringFromLong(it) },
            isActive = isActive,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate)!!,
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate)!!,
            mpaFile = null,
            dischargeSummary1 = null,
            dischargeSummary2 = null,
        )
    }
}

@JsonClass(generateAdapter = true)
data class ECTNetwork(
    val id: Int? = null,
    @SerializedName(value = "benId", alternate = ["beneficiaryID", "beneficiaryId", "beneficiaryRegID", "beneficiaryRegId", "benID"])
    val benId: Long? = null,
    @SerializedName(value = "visitDate", alternate = ["visitdate"])
    val visitDate: String? = null,
    val financialYear: String? = null,
    val visitMonth: String? = null,
    val lmpDate: String? = null,
    val isPregnancyTestDone: String? = null,
    val pregnancyTestResult: String? = null,
    val isPregnant: String? = null,
    val usingFamilyPlanning: Boolean? = null,
    val methodOfContraception: String? = null,
    val anyOtherMethod: String? = null,
    val antraDose: String? = null,
    @SerializedName(value = "dateOfAntraInjection", alternate = ["antraInjectionDate"])
    val dateOfAntraInjection: String? = null,
    @SerializedName(value = "dueDateOfAntraInjection", alternate = ["antraDueDate"])
    val dueDateOfAntraInjection: String? = null,
    val dateOfSterilization: String? = null,
    var isActive: Boolean? = null,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val updatedBy: String? = null,
    val updatedDate: String? = null,
    val mpaFile: String? = null,
    val dischargeSummary1: String? = null,
    val dischargeSummary2: String? = null,
) {
    /**
     * Convert server response into a local [EligibleCoupleTrackingCache] entity.
     * @param patientID local patient ID mapped from [benId].
     */
    fun toCache(patientID: String): EligibleCoupleTrackingCache {
        return EligibleCoupleTrackingCache(
            patientID = patientID,
            visitDate = org.piramalswasthya.cho.network.getLongFromDate(visitDate),
            financialYear = financialYear,
            visitMonth = visitMonth,
            lmpDate = lmpDate?.let { org.piramalswasthya.cho.network.getLongFromDate(it) },
            isPregnancyTestDone = isPregnancyTestDone,
            pregnancyTestResult = pregnancyTestResult,
            isPregnant = isPregnant,
            usingFamilyPlanning = usingFamilyPlanning,
            methodOfContraception = methodOfContraception,
            anyOtherMethod = anyOtherMethod,
            antraDose = antraDose,
            antraInjectionDate = dateOfAntraInjection?.let { org.piramalswasthya.cho.network.getLongFromDate(it) },
            antraDueDate = dueDateOfAntraInjection?.let { org.piramalswasthya.cho.network.getLongFromDate(it) },
            dateOfSterilization = dateOfSterilization?.let { org.piramalswasthya.cho.network.getLongFromDate(it) },
            createdBy = createdBy ?: "system",
            createdDate = org.piramalswasthya.cho.network.getLongFromDate(createdDate),
            updatedBy = updatedBy ?: (createdBy ?: "system"),
            updatedDate = org.piramalswasthya.cho.network.getLongFromDate(updatedDate),
            processed = "P",
            isActive = isActive ?: true,
            syncState = org.piramalswasthya.cho.database.room.SyncState.SYNCED
        )
    }
}
