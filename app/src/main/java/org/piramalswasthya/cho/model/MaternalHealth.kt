package org.piramalswasthya.cho.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.helpers.getDateString
import org.piramalswasthya.cho.helpers.getTodayMillis
import org.piramalswasthya.cho.helpers.getWeeksOfPregnancy
import org.piramalswasthya.cho.network.getLongFromDate
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.HelperUtil.getDateStringFromLong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


data class PregnantWomenVisitCache(
    val benId: Long,
    val name: String,
    val dob: Long,
    val mobileNo: Long,
    val rchId: String? = null,
    val familyHeadName: String? = null,
    val spouseName: String,
    val lmp: Long,
) {
    fun asDomainModel() =
        PregnantWomenVisitDomain(
            benId = benId,
            name = name,
            age = " Years",
            familyHeadName = familyHeadName ?: "Not Available",
            spouseName = spouseName,
            mobileNo = mobileNo.toString(),
            rchId = rchId?.takeIf { it.isNotBlank() } ?: "Not Available",
            lmp = lmp,
            weeksOfPregnancy = getWeeksOfPregnancy(System.currentTimeMillis(), lmp)
        )

}

data class PregnantWomenVisitDomain(
    val benId: Long,
    val name: String,
    val age: String,
    val familyHeadName: String,
    val spouseName: String,
    val mobileNo: String,
    val rchId: String,
    val lmp: Long,
    val lmpString: String? = getDateString(lmp),
    val edd: Long = lmp + TimeUnit.DAYS.toMillis(280),
    val eddString: String? = getDateString(edd),
    val weeksOfPregnancy: Int,
    val weeksOfPregnancyString: String = if (weeksOfPregnancy <= 40) weeksOfPregnancy.toString() else "NA",
) {

}

//data class AncStatus(
//    val benId: Long,
//    val visitNumber: Int,
//    val filledWeek: Int,
//    val syncState: SyncState? = null
//)

enum class AncFormState {
    ALLOW_FILL,
    ALREADY_FILLED,
    NO_FILL
}

@Entity(
    tableName = "PREGNANCY_REGISTER",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"/* "householdId"*/),
        childColumns = arrayOf("patientID" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_pwc", value = ["patientID"/* "hhId"*/])]
)

data class PregnantWomanRegistrationCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientID: String,
    var dateOfRegistration: Long = System.currentTimeMillis(),
    var mcpCardNumber: Long? = 0,
    var rchId: Long? = 0,
    var lmpDate: Long = 0,
//    var weeksOfPregnancy : String,
//    var weeksOfPregnancyId : Int,
//    var expectedDateOfDelivery : Long,
    var bloodGroup: String? = null,
    var bloodGroupId: Int = 0,
    var weight: Int? = null,
    var height: Int? = null,
    var vdrlRprTestResult: String? = null,
    var vdrlRprTestResultId: Int = 0,
    var dateOfVdrlRprTest: Long? = null,

    var hivTestResult: String? = null,
    var hivTestResultId: Int = 0,
    var dateOfHivTest: Long? = null,

    var hbsAgTestResult: String? = null,
    var hbsAgTestResultId: Int = 0,
    var dateOfHbsAgTest: Long? = null,

    var pastIllness: String? = null,
    var otherPastIllness: String? = null,
    var is1st: Boolean = true,
    var numPrevPregnancy: Int? = null,
    var complicationPrevPregnancy: String? = null,
    var complicationPrevPregnancyId: Int? = null,
    var otherComplication: String? = null,
    var isHrp: Boolean = false,
    var hrpIdBy: String? = null,
    var hrpIdById: Int = 0,
    var active: Boolean = true,
    var tt1: Long? = null,
    var tt2: Long? = null,
    var ttBooster: Long? = null,
    var processed: String? = "N",
    var createdBy: String,
    var createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {

    fun getDateStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        dateLong?.let {
            return dateFormat.format(dateLong)
        } ?: run {
            return null
        }
    }

    fun asPwrPost(): PwrPost {
        return PwrPost(
//            benId = benId,
            registrationDate = getDateStringFromLong(dateOfRegistration),
            rchId = rchId,
            mcpCardId = mcpCardNumber,
            lmpDate = getDateStringFromLong(lmpDate),
            bloodGroup = bloodGroup,
            weight = weight,
            height = height,
            rprTestResult = vdrlRprTestResult,
            dateOfRprTest = dateOfVdrlRprTest?.let { getDateStringFromLong(it) },
            hivTestResult = hivTestResult,
            hbsAgTestResult = hbsAgTestResult,
            dateOfHivTest = dateOfHivTest?.let { getDateStringFromLong(it) },
            dateOfHbsAgTest = dateOfHbsAgTest?.let { getDateStringFromLong(it) },
            pastIllness = pastIllness,
            otherPastIllness = otherPastIllness,
            isFirstPregnancyTest = is1st,
            numPrevPregnancy = numPrevPregnancy,
            pregComplication = complicationPrevPregnancy,
            otherComplication = otherComplication,
            tdDose1Date = tt1?.let { getDateStringFromLong(it) },
            tdDose2Date = tt2?.let { getDateStringFromLong(it) },
            tdDoseBoosterDate = ttBooster?.let { getDateStringFromLong(it) },
            isActive = active,
            isHrpCase = isHrp,
            assignedAsHrpBy = hrpIdBy,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

/**
 * Patient with Pregnant Woman Registration data
 */
data class PatientWithPwrCache(
    @Embedded
    val patient: Patient,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val pwr: PregnantWomanRegistrationCache?
) {
    fun asDomainModel(): PatientWithPwrDomain {
        return PatientWithPwrDomain(
            patient = patient,
            pwr = pwr
        )
    }
}

/**
 * Domain model for displaying patient with pregnancy registration
 */
data class PatientWithPwrDomain(
    val patient: Patient,
    val pwr: PregnantWomanRegistrationCache?
) {
    /**
     * Get weeks of pregnancy
     * Returns 0 if LMP date is missing or invalid (null or <= 0)
     */
    fun getWeeksOfPregnancy(): Int {
        return if (pwr?.lmpDate != null && pwr.lmpDate > 0L) {
            val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - pwr.lmpDate)
            (daysSinceLMP / 7).toInt()
        } else {
            0
        }
    }

    /**
     * Get EDD (Expected Date of Delivery) - LMP + 280 days
     * Returns 0L if LMP date is missing or invalid (null or <= 0)
     */
    fun getEDD(): Long {
        return if (pwr?.lmpDate != null && pwr.lmpDate > 0L) {
            pwr.lmpDate + TimeUnit.DAYS.toMillis(280)
        } else {
            0L
        }
    }

    /**
     * Get formatted LMP date string
     * Returns "NA" if LMP date is missing or invalid (null or <= 0)
     */
    fun getFormattedLMPDate(): String {
        return if (pwr?.lmpDate != null && pwr.lmpDate > 0L) {
            getDateStringFromLong(pwr.lmpDate) ?: "NA"
        } else {
            "NA"
        }
    }

    /**
     * Get formatted EDD string
     * Returns "NA" if LMP date is missing or invalid (null or <= 0)
     */
    fun getFormattedEDD(): String {
        val edd = getEDD()
        return if (edd > 0L) {
            getDateStringFromLong(edd) ?: "NA"
        } else {
            "NA"
        }
    }

    /**
     * Get patient's age string for display (e.g. "29 YEARS" or "NA").
     */
    fun getAgeString(): String {
        return patient.dob?.let { DateTimeUtil.calculateAgeString(it) } ?: "NA"
    }

    /**
     * Check if pregnancy is active
     */
    fun isActive(): Boolean {
        return pwr?.active ?: false
    }

    /**
     * Check if this is high-risk pregnancy
     */
    fun isHighRisk(): Boolean {
        return pwr?.isHrp ?: false
    }
}

/**
 * Patient with PWR and ANC records (for abortion list)
 */
data class PatientWithPwrAndAncCache(
    @Embedded
    val patient: Patient,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val pwr: PregnantWomanRegistrationCache?,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val ancRecords: List<PregnantWomanAncCache>
) {
    fun asAbortionDomainModel(): AbortionDomain {
        val abortionRecord = ancRecords.firstOrNull { it.isAborted && it.abortionDate != null }
        val activePwr = pwr?.takeIf { it.active }
        val registeredPwr = pwr
        
        // Use LMP from registered PWR (even if inactive) or abortion record
        val lmpDateToUse = registeredPwr?.lmpDate
            ?: abortionRecord?.let { 
                // If no PWR, try to get LMP from abortion record (if stored)
                // For now, use 0L if not available
                0L
            } ?: 0L

        val eddDateToUse = if (lmpDateToUse != 0L) {
            lmpDateToUse + TimeUnit.DAYS.toMillis(280)
        } else 0L

        val weekOfPregnancyToUse = if (lmpDateToUse != 0L) {
            (TimeUnit.MILLISECONDS.toDays(getTodayMillis() - lmpDateToUse) / 7).toInt()
        } else null

        return AbortionDomain(
            patient = patient,
            pwr = activePwr,
            abortionRecord = abortionRecord,
            lmpDate = lmpDateToUse,
            eddDate = eddDateToUse,
            weekOfPregnancy = weekOfPregnancyToUse,
            abortionDate = abortionRecord?.abortionDate
        )
    }
}

/**
 * Domain model for displaying abortion list
 */
data class AbortionDomain(
    val patient: Patient,
    val pwr: PregnantWomanRegistrationCache?,
    val abortionRecord: PregnantWomanAncCache?,
    val lmpDate: Long,
    val eddDate: Long,
    val weekOfPregnancy: Int?,
    val abortionDate: Long?
) {
    /**
     * Get formatted LMP date string
     */
    fun getFormattedLMPDate(): String {
        return if (lmpDate != 0L) {
            getDateStringFromLong(lmpDate) ?: "NA"
        } else "NA"
    }

    /**
     * Get formatted EDD string
     */
    fun getFormattedEDD(): String {
        return if (eddDate != 0L) {
            getDateStringFromLong(eddDate) ?: "NA"
        } else "NA"
    }

    /**
     * Get formatted abortion date string
     */
    fun getFormattedAbortionDate(): String {
        return abortionDate?.let { getDateStringFromLong(it) ?: "NA" } ?: "NA"
    }

    /**
     * Get weeks of pregnancy string
     */
    fun getWeeksOfPregnancyString(): String {
        return weekOfPregnancy?.takeIf { it <= 40 }?.toString() ?: "NA"
    }

    /**
     * Get age string from patient DOB for display (e.g. "25 YEARS" or "NA").
     */
    fun getAgeString(): String {
        return patient.dob?.let { DateTimeUtil.calculateAgeString(it) } ?: "NA"
    }

    /**
     * Check if abortion form is filled (has abortion type and facility)
     */
    val isAbortionFormFilled: Boolean
        get() = abortionRecord?.let { 
            it.abortionType != null && it.abortionFacility != null 
        } ?: false
}

/**
 * Patient with PWR for PMSMA list
 * Shows women registered for pregnancy (eligible for PMSMA)
 */
data class PatientWithPwrForPmsmaCache(
    @Embedded
    val patient: Patient,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val pwr: PregnantWomanRegistrationCache?
) {
    fun asPmsmaDomainModel(): PmsmaDomain {
        val activePwr = pwr?.takeIf { it.active }
        
        val lmpDateToUse = activePwr?.lmpDate ?: 0L
        val eddDateToUse = if (lmpDateToUse != 0L) {
            lmpDateToUse + TimeUnit.DAYS.toMillis(280)
        } else 0L

        val weekOfPregnancyToUse = if (lmpDateToUse != 0L) {
            (TimeUnit.MILLISECONDS.toDays(getTodayMillis() - lmpDateToUse) / 7).toInt()
        } else null

        return PmsmaDomain(
            patient = patient,
            pwr = activePwr,
            lmpDate = lmpDateToUse,
            eddDate = eddDateToUse,
            weekOfPregnancy = weekOfPregnancyToUse
        )
    }
}

/**
 * Domain model for displaying PMSMA list
 */
data class PmsmaDomain(
    val patient: Patient,
    val pwr: PregnantWomanRegistrationCache?,
    val lmpDate: Long,
    val eddDate: Long,
    val weekOfPregnancy: Int?
) {
    /**
     * Get formatted LMP date string
     */
    fun getFormattedLMPDate(): String {
        return if (lmpDate != 0L) {
            getDateStringFromLong(lmpDate) ?: "NA"
        } else "NA"
    }

    /**
     * Get formatted EDD string
     */
    fun getFormattedEDD(): String {
        return if (eddDate != 0L) {
            getDateStringFromLong(eddDate) ?: "NA"
        } else "NA"
    }

    /**
     * Get weeks of pregnancy string
     */
    fun getWeeksOfPregnancyString(): String {
        return weekOfPregnancy?.takeIf { it <= 40 }?.toString() ?: "NA"
    }

    /**
     * Get patient's age string for display (e.g. "30 YEARS" or "NA").
     */
    fun getAgeString(): String {
        return patient.dob?.let { DateTimeUtil.calculateAgeString(it) } ?: "NA"
    }
}

data class PwrPost(
    val id: Long = 0,
    val benId: Long = 0,
    val registrationDate: String? = null,
    val rchId: Long? = null,
    val mcpCardId: Long? = null,
    var lmpDate: String? = null,
    val bloodGroup: String? = null,
    val weight: Int? = null,
    val height: Int? = null,
    val rprTestResult: String? = null,
    val dateOfRprTest: String? = null,
    val hivTestResult: String? = null,
    val hbsAgTestResult: String? = null,
    val dateOfHivTest: String? = null,
    val dateOfHbsAgTest: String? = null,
    val pastIllness: String? = null,
    val otherPastIllness: String? = null,
    var isFirstPregnancyTest: Boolean = true,
    val numPrevPregnancy: Int? = null,
    val pregComplication: String? = null,
    val otherComplication: String? = null,
    var isRegistered: Boolean = true,
    var rhNegative: String? = null,
    var homeDelivery: String? = null,
    var badObstetric: String? = null,
    var isHrpCase: Boolean = false,
    var assignedAsHrpBy: String? = null,
    val tdDose1Date: String? = null,
    val tdDose2Date: String? = null,
    val tdDoseBoosterDate: String? = null,
    val isActive: Boolean,
    val createdDate: String? = null,
    val createdBy: String,
    var updatedDate: String? = null,
    var updatedBy: String
) {
    fun toPwrCache(): PregnantWomanRegistrationCache {
        return PregnantWomanRegistrationCache(
            id = id,
            patientID = "",
            dateOfRegistration = getLongFromDate(registrationDate),
            mcpCardNumber = mcpCardId,
            rchId = rchId,
            lmpDate = getLongFromDate(lmpDate),
            bloodGroup = bloodGroup,
//            bloodGroupId =
            weight = weight,
            height = height,
            vdrlRprTestResult = rprTestResult,
//            vdrlRprTestResultId
            dateOfVdrlRprTest = dateOfRprTest?.let { getLongFromDate(it) },
            hivTestResult = hivTestResult,
//            hivTestResultId
            dateOfHivTest = dateOfHivTest?.let { getLongFromDate(it) },
            hbsAgTestResult = hbsAgTestResult,
//            hbsAgTestResultId
            dateOfHbsAgTest = dateOfHbsAgTest?.let { getLongFromDate(it) },
            pastIllness = pastIllness,
            otherPastIllness = otherPastIllness,
            is1st = isFirstPregnancyTest,
            numPrevPregnancy = numPrevPregnancy,
            complicationPrevPregnancy = pregComplication,
            otherComplication = otherComplication,
            isHrp = isHrpCase,
            hrpIdBy = assignedAsHrpBy,
            tt1 = getLongFromDate(tdDose1Date),
            tt2 = getLongFromDate(tdDose2Date),
            ttBooster = getLongFromDate(tdDoseBoosterDate),
            active = isActive,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}


@Entity(
    tableName = "PREGNANCY_ANC",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"/* "householdId"*/),
        childColumns = arrayOf("patientID" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_mha", value = ["patientID"/* "hhId"*/])],
)

data class PregnantWomanAncCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientID: String,
    var visitNumber: Int,
    var isActive: Boolean = true,
    var ancDate: Long = System.currentTimeMillis(),
    var isAborted: Boolean = false,
    var abortionType: String? = null,
    var abortionTypeId: Int = 0,
    var abortionFacility: String? = null,
    var abortionFacilityId: Int = 0,
    var abortionDate: Long? = null,
    var weight: Int? = null,
    var bpSystolic: Int? = null,
    var bpDiastolic: Int? = null,
    var pulseRate: String? = null,
    var hb: Double? = null,
    var fundalHeight: Int? = null,
    var urineAlbumin: String? = null,
    var urineAlbuminId: Int = 0,
    var randomBloodSugarTest: String? = null,
    var randomBloodSugarTestId: Int = 0,
    var numFolicAcidTabGiven: Int = 0,
    var numIfaAcidTabGiven: Int = 0,
    var anyHighRisk: Boolean? = null,
    var highRisk: String? = null,
    var highRiskId: Int = 0,
    var otherHighRisk: String? = null,
    var referralFacility: String? = null,
    var referralFacilityId: Int = 0,
    var hrpConfirmed: Boolean? = null,
    var hrpConfirmedBy: String? = null,
    var hrpConfirmedById: Int = 0,
    var maternalDeath: Boolean? = null,
    var maternalDeathProbableCause: String? = null,
    var maternalDeathProbableCauseId: Int = 0,
    var otherMaternalDeathProbableCause: String? = null,
    var deathDate: Long? = null,
    var pregnantWomanDelivered: Boolean? = null,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    fun asPostModel(benId: Long): ANCPost {
        return ANCPost(
            benId = benId,
            ancDate = getDateStringFromLong(ancDate),
            isActive = true,
            ancVisit = visitNumber,
            isAborted = isAborted,
            abortionType = abortionType,
            abortionFacility = abortionFacility,
            abortionDate = abortionDate?.let { getDateStringFromLong(it) },
            weightOfPW = weight,
            bpSystolic = bpSystolic,
            bpDiastolic = bpDiastolic,
            pulseRate = pulseRate?.toInt(),
            hb = hb,
            fundalHeight = fundalHeight,
            urineAlbuminPresent = urineAlbumin == "Present",
            bloodSugarTestDone = randomBloodSugarTest == "Done",
            folicAcidTabs = numFolicAcidTabGiven,
            ifaTabs = numIfaAcidTabGiven,
            isHighRisk = anyHighRisk,
            highRiskCondition = highRisk,
            otherHighRiskCondition = otherHighRisk,
            referralFacility = referralFacility,
            isHrpConfirmed = hrpConfirmed,
            hrpIdentifiedBy = hrpConfirmedBy,
            isMaternalDeath = maternalDeath,
            probableCauseOfDeath = maternalDeathProbableCause,
            otherCauseOfDeath = otherMaternalDeathProbableCause,
            deathDate = deathDate?.let { getDateStringFromLong(it) },
            isBabyDelivered = pregnantWomanDelivered,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

data class ANCPost(
    val id: Long = 0,
    val benId: Long = 0,
    val ancDate: String? = null,
    val isActive: Boolean,
    val ancVisit: Int,
    val isAborted: Boolean = false,
    val abortionType: String? = null,
    val abortionFacility: String? = null,
    val abortionDate: String? = null,
    val weightOfPW: Int? = null,
    val bpSystolic: Int? = null,
    val bpDiastolic: Int? = null,
    val pulseRate: Int? = null,
    val hb: Double? = null,
    val fundalHeight: Int? = null,
    val urineAlbuminPresent: Boolean? = null,
    val bloodSugarTestDone: Boolean? = null,

    val folicAcidTabs: Int = 0,
    val ifaTabs: Int = 0,
    val isHighRisk: Boolean? = null,
    val highRiskCondition: String? = null,
    val otherHighRiskCondition: String? = null,
    val referralFacility: String? = null,
    val isHrpConfirmed: Boolean? = null,
    val hrpIdentifiedBy: String? = null,
    val isMaternalDeath: Boolean? = null,
    val probableCauseOfDeath: String? = null,
    val otherCauseOfDeath: String? = null,
    val deathDate: String? = null,
    val isBabyDelivered: Boolean? = null,
    val createdDate: String? = null,
    val createdBy: String,
    val updatedDate: String? = null,
    val updatedBy: String
) {
    fun toAncCache(): PregnantWomanAncCache {
        return PregnantWomanAncCache(
            id = id,
            patientID = "",
            visitNumber = ancVisit,
            ancDate = getLongFromDate(ancDate),
            isAborted = isAborted,
            abortionType = abortionType,
//            abortionTypeId =
            abortionFacility = abortionFacility,
//            abortionFacilityId
            abortionDate = getLongFromDate(abortionDate),
            weight = weightOfPW,
            bpSystolic = bpSystolic,
            bpDiastolic = bpDiastolic,
            pulseRate = pulseRate.toString(),
            hb = hb,
            fundalHeight = fundalHeight,
            urineAlbumin = if (urineAlbuminPresent == true) "Present" else "Absent",
//            urineAlbuminId
            randomBloodSugarTest = if (bloodSugarTestDone == true) "Done" else "Not Done",
//            randomBloodSugarTestId
            numFolicAcidTabGiven = folicAcidTabs,
            numIfaAcidTabGiven = ifaTabs,
            anyHighRisk = isHighRisk,
            highRisk = highRiskCondition,
//            highRiskId
            otherHighRisk = otherHighRiskCondition,
            referralFacility = referralFacility,
//            referralFacilityId
//            hrpConfirmed
//            hrpConfirmedBy
//            hrpConfirmedById
            maternalDeath = isMaternalDeath,
            maternalDeathProbableCause = probableCauseOfDeath,
//            maternalDeathProbableCauseId
            otherMaternalDeathProbableCause = otherCauseOfDeath,
            deathDate = getLongFromDate(deathDate),
            pregnantWomanDelivered = isBabyDelivered,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
}


//data class BenWithAncVisitCache(
////    @ColumnInfo(name = "benId")
////    val ecBenId: Long,
//
//    @Embedded
//    val ben: BenBasicCache,
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId"
//    )
//    val pwr: List<PregnantWomanRegistrationCache>,
//
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId", entity = PMSMACache::class
//    )
//    val pmsma: List<PMSMACache>,
//
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId", entity = PregnantWomanAncCache::class
//    )
//    val savedAncRecords: List<PregnantWomanAncCache>
//) {
//
//    companion object {
//        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
//
//        private fun getAncVisitedDateFromLong(long: Long): String {
//            return "Visited on ${dateFormat.format(long)}"
//        }
//    }
//
//    fun asDomainModel(): BenWithAncListDomain {
//        val lastAncRecord = savedAncRecords.maxByOrNull { it.ancDate }
//        val activePmsma = pmsma.firstOrNull { it.isActive }
//        val activePwrRecrod = pwr.first { it.active }
//        return BenWithAncListDomain(
////            ecBenId,
//            ben.asBasicDomainModel(),
//            activePwrRecrod,
//            savedAncRecords.filter { it.isActive }.map {
//                AncStatus(
//                    benId = it.benId,
//                    visitNumber = it.visitNumber,
//                    filledWeek = (TimeUnit.MILLISECONDS.toDays(it.ancDate - activePwrRecrod.lmpDate) / 7).toInt(),
//                    syncState = it.syncState
//                )
//            }.sortedBy { it.visitNumber },
//            pmsmaFillable = if (activePmsma == null) savedAncRecords.any { it.visitNumber == 1 } else true,
//            hasPmsma = activePmsma != null,
//            showAddAnc = if (savedAncRecords.isEmpty())
//                TimeUnit.MILLISECONDS.toDays(
//                    getTodayMillis() - activePwrRecrod.lmpDate
//                ) >= Konstants.minAnc1Week * 7
//            else
//                lastAncRecord != null &&
//                        (activePwrRecrod.lmpDate + TimeUnit.DAYS.toMillis(280)) > (lastAncRecord.ancDate + TimeUnit.DAYS.toMillis(
//                    28
//                )) &&
//                        lastAncRecord.visitNumber < 4 && TimeUnit.MILLISECONDS.toDays(
//                    getTodayMillis() - lastAncRecord.ancDate
//                ) > 28,
//            syncState = if (activePmsma == null && savedAncRecords.isEmpty()) null else if (activePmsma?.syncState == SyncState.UNSYNCED || savedAncRecords.any { it.syncState != SyncState.SYNCED }) SyncState.UNSYNCED else SyncState.SYNCED
//        )
//
//
//    }
//}

//data class BenWithAncListDomain(
//    val ben: BenBasicDomain,
//    val pwr: PregnantWomanRegistrationCache,
//    val anc: List<AncStatus>,
//    val lmpString: String? = getDateString(pwr.lmpDate),
//    val eddString: String? = getDateString(pwr.lmpDate + TimeUnit.DAYS.toMillis(280)),
//    val weeksOfPregnancy: String? = (TimeUnit.MILLISECONDS.toDays(getTodayMillis() - pwr.lmpDate) / 7).takeIf { it <= 40 }
//        ?.toString() ?: "NA",
//    val showAddAnc: Boolean,
//    val pmsmaFillable: Boolean,
//    val hasPmsma: Boolean,
//    val showViewAnc: Boolean = anc.isEmpty(),
//    val syncState: SyncState?
//)