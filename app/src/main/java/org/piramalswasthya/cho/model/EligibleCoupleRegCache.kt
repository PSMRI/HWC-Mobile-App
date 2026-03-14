package org.piramalswasthya.cho.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.Json
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.HelperUtil
import java.util.concurrent.TimeUnit

/**
 * Eligible Couple Registration Cache
 * Stores registration data for eligible couples in the RMNCHA+ module
 */
@Entity(
    tableName = "ELIGIBLE_COUPLE_REG",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"),
        childColumns = arrayOf("patientID"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ecrInd", value = ["patientID"])]
)
data class EligibleCoupleRegCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientID: String,
    var dateOfReg: Long = System.currentTimeMillis(),
    var lmpDate: Long? = null,
    var noOfChildren: Int = 0,
    var noOfLiveChildren: Int = 0,
    var noOfMaleChildren: Int = 0,
    var noOfFemaleChildren: Int = 0,
    var isRegistered: Boolean = true,
    var processed: String? = "N",
    var createdBy: String,
    var createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel

/**
 * Patient with Eligible Couple Registration data
 */
data class PatientWithECRCache(
    @Embedded
    val patient: Patient,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val ecr: EligibleCoupleRegCache?
) {
    fun asDomainModel(): PatientWithEcrDomain {
        return PatientWithEcrDomain(
            patient = patient,
            ecr = ecr
        )
    }
}

/**
 * Domain model for displaying patient with EC registration
 */
data class PatientWithEcrDomain(
    val patient: Patient,
    val ecr: EligibleCoupleRegCache?
) {
    /**
     * Calculate EC status based on LMP date
     * Returns "Missed Period" if LMP date is > 35 days ago, otherwise "Under Review"
     */
    fun getECStatus(): String {
        val finalLmpDate = ecr?.lmpDate ?: lmpDateFromTracking
        return if (finalLmpDate != null && finalLmpDate > 0L) {
            val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(
                System.currentTimeMillis() - finalLmpDate
            )
            if (daysSinceLMP > 35) "Missed Period" else "Under Review"
        } else {
            "Under Review"
        }
    }

    /**
     * Check if the missed period indicator should be shown (red icon)
     */
    fun showMissedPeriodIndicator(): Boolean {
        return ecr?.lmpDate?.let {
            val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it)
            daysSinceLMP > 35
        } ?: false
    }

    /**
     * Get formatted LMP date string
     */
    fun getFormattedLMPDate(): String {
        val finalLmpDate = ecr?.lmpDate ?: lmpDateFromTracking
        return if (finalLmpDate != null && finalLmpDate > 0L) {
            HelperUtil.getDateStringFromLong(finalLmpDate) ?: "NA"
        } else {
            "NA"
        }
    }

    /**
     * Get patient's age string for display (e.g. "30 YEARS" or "NA").
     */
    fun getAgeString(): String {
        return patient.dob?.let { DateTimeUtil.calculateAgeString(it) } ?: "NA"
    }

    /**
     * Get last visit date string for display.
     * This will be set by the adapter/fragment after loading ECT data.
     */
    var lastVisitDate: Long? = null
    var methodOfContraception: String? = null
    var antraNextDueDate: Long? = null
    var antraInjectionDate: Long? = null
    var lmpDateFromTracking: Long? = null

    /**
     * Get formatted last visit date string
     */
    fun getLastVisitDateString(): String {
        return lastVisitDate?.let { visitDate ->
            if (visitDate > 0L) HelperUtil.getDateStringFromLong(visitDate) ?: "NA"
            else "NA"
        } ?: "NA"
    }

    /**
     * Get formatted ANTRA next due date string (range if possible)
     */
    fun getAntraDueDateString(): String {
        return antraInjectionDate?.let { injectionDate ->
            if (injectionDate > 0L) {
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = injectionDate

                cal.add(java.util.Calendar.DAY_OF_YEAR, 76)
                val startDate = HelperUtil.getDateStringFromLong(cal.timeInMillis)

                cal.timeInMillis = injectionDate
                cal.add(java.util.Calendar.DAY_OF_YEAR, 120)
                val endDate = HelperUtil.getDateStringFromLong(cal.timeInMillis)

                if (startDate != null && endDate != null) "$startDate to $endDate"
                else startDate ?: endDate ?: "NA"
            } else "NA"
        } ?: antraNextDueDate?.let { dueDate ->
            if (dueDate > 0L) HelperUtil.getDateStringFromLong(dueDate) ?: "NA"
            else "NA"
        } ?: "NA"
    }
}

/**
 * Network model for syncing EC registration data
 */
data class EcrPost(
    val benId: Long,
    @Json(name = "registrationDate")
    val dateOfReg: String? = null,
    val lmpDate: String? = null,
    val numChildren: Int? = null,
    val numLiveChildren: Int? = null,
    val numMaleChildren: Int? = null,
    val numFemaleChildren: Int? = null,
    var isRegistered: Boolean = true,
    var createdBy: String,
    val createdDate: String,
    var updatedBy: String,
    val updatedDate: String
)