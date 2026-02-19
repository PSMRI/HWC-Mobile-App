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

/**
 * Neonatal Outcome entity to track detailed newborn health information
 * Linked to DeliveryOutcomeCache via deliveryOutcomeId
 * Supports multiple neonates per delivery via neonateIndex
 */
@Entity(
    tableName = "NEONATAL_OUTCOME",
    foreignKeys = [ForeignKey(
        entity = DeliveryOutcomeCache::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("deliveryOutcomeId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "neonatalOutcomeInd", value = ["deliveryOutcomeId"])]
)
data class NeonatalOutcomeCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Foreign key to DELIVERY_OUTCOME table */
    val deliveryOutcomeId: Long,
    
    /** Index of this neonate (1 for 1st baby, 2 for 2nd baby in twins, etc.) */
    val neonateIndex: Int,
    
    /** Unique Neonate ID generated for tracking (optional, for frontend display) */
    var neonateUniqueId: String? = null,
    
    // Q1: Number of neonates is stored in delivery outcome (liveBirth + stillBirth)
    
    // Q2: Outcome at Birth
    /** Live Birth, Still Birth (Macerated), Still Birth (Fresh), Died during delivery */
    var outcomeAtBirth: String? = null,
    var outcomeAtBirthId: Int? = null,
    
    // Q3: Sex
    /** Male, Female, Ambiguous */
    var sex: String? = null,
    var sexId: Int? = null,
    
    // Q4: Cried immediately after birth?
    /** Immediate cry, Cried after resuscitation, Not applicable (Stillbirth) */
    var criedImmediately: String? = null,
    var criedImmediatelyId: Int? = null,
    
    // Q5: Type of resuscitation (multi-select, comma-separated)
    /** Stimulation, Suctioning, Bag and mask ventilation, Oxygen, Intubation, Chest compressions, Medications */
    var typeOfResuscitation: String? = null,
    
    // Q6: Birth Weight (in grams)
    var birthWeight: Int? = null,
    
    // Q7: Any congenital anomaly detected?
    /** Yes, No, Suspected (under evaluation) */
    var congenitalAnomalyDetected: String? = null,
    var congenitalAnomalyDetectedId: Int? = null,
    
    // Q8: Type of congenital anomaly (multi-select, comma-separated)
    /** Neural tube defect, Cleft lip/palate, Club foot, Down syndrome, Congenital heart defect, etc. */
    var typeOfCongenitalAnomaly: String? = null,
    
    // Q9: Other congenital anomaly (if "Other" selected in Q8)
    var otherCongenitalAnomaly: String? = null,
    
    // Q10: Newborn Complications (multi-select, comma-separated)
    /** Birth asphyxia, Respiratory distress, Neonatal jaundice, Sepsis, Hypothermia, etc. */
    var newbornComplications: String? = null,
    
    // Q11: Current Status of Baby
    /** Healthy and with mother, Admitted (SNCU/NICU), Admitted (General ward), Died */
    var currentStatusOfBaby: String? = null,
    var currentStatusOfBabyId: Int? = null,
    
    // Q12: If baby died, cause of death (multi-select, comma-separated)
    /** Birth asphyxia, Prematurity, Low birth weight complications, Sepsis, etc. */
    var causeOfDeath: String? = null,
    
    // Q13: Other cause of death (if "Other" selected in Q12)
    var otherCauseOfDeath: String? = null,
    
    // Q14: Birth dose vaccines given (multi-select, comma-separated)
    /** BCG, Hepatitis B (Birth dose), OPV-0, None */
    var birthDoseVaccinesGiven: String? = null,
    
    // Q15: Reason for not giving birth dose vaccines (if Q14 = None)
    var reasonForNoVaccines: String? = null,
    
    // Q16: Vitamin K injection given?
    /** Yes, No */
    var vitaminKInjectionGiven: Boolean? = null,
    
    // Q17: Reason for not giving Vitamin K injection (if Q16 = No)
    var reasonForNoVitaminK: String? = null,
    
    // Q18: Birth Certificate issued?
    /** Yes, In process, No (Not applied) */
    var birthCertificateIssued: String? = null,
    var birthCertificateIssuedId: Int? = null,
    
    // Audit flags
    /** True if stillbirth or died during delivery (triggers stillbirth audit) */
    var isStillbirth: Boolean? = null,
    
    /** True if baby died after birth (triggers neonatal death audit) */
    var isNeonatalDeath: Boolean? = null,
    
    // Sync and metadata fields
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {
    
    fun getDateStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return dateLong?.let { dateFormat.format(it) }
    }
    
    fun asPostModel(benId: Long): NeonatalOutcomePost {
        return NeonatalOutcomePost(
            id = id,
            benId = benId,
            deliveryOutcomeId = deliveryOutcomeId,
            neonateIndex = neonateIndex,
            neonateUniqueId = neonateUniqueId,
            outcomeAtBirth = outcomeAtBirth,
            outcomeAtBirthId = outcomeAtBirthId,
            sex = sex,
            sexId = sexId,
            criedImmediately = criedImmediately,
            criedImmediatelyId = criedImmediatelyId,
            typeOfResuscitation = typeOfResuscitation,
            birthWeight = birthWeight,
            congenitalAnomalyDetected = congenitalAnomalyDetected,
            congenitalAnomalyDetectedId = congenitalAnomalyDetectedId,
            typeOfCongenitalAnomaly = typeOfCongenitalAnomaly,
            otherCongenitalAnomaly = otherCongenitalAnomaly,
            newbornComplications = newbornComplications,
            currentStatusOfBaby = currentStatusOfBaby,
            currentStatusOfBabyId = currentStatusOfBabyId,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            birthDoseVaccinesGiven = birthDoseVaccinesGiven,
            reasonForNoVaccines = reasonForNoVaccines,
            vitaminKInjectionGiven = vitaminKInjectionGiven,
            reasonForNoVitaminK = reasonForNoVitaminK,
            birthCertificateIssued = birthCertificateIssued,
            birthCertificateIssuedId = birthCertificateIssuedId,
            isStillbirth = isStillbirth,
            isNeonatalDeath = isNeonatalDeath,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }
}

/**
 * Post model for syncing to server
 */
data class NeonatalOutcomePost(
    val id: Long = 0,
    val benId: Long,
    val deliveryOutcomeId: Long,
    val neonateIndex: Int,
    val neonateUniqueId: String? = null,
    val outcomeAtBirth: String? = null,
    val outcomeAtBirthId: Int? = null,
    val sex: String? = null,
    val sexId: Int? = null,
    val criedImmediately: String? = null,
    val criedImmediatelyId: Int? = null,
    val typeOfResuscitation: String? = null,
    val birthWeight: Int? = null,
    val congenitalAnomalyDetected: String? = null,
    val congenitalAnomalyDetectedId: Int? = null,
    val typeOfCongenitalAnomaly: String? = null,
    val otherCongenitalAnomaly: String? = null,
    val newbornComplications: String? = null,
    val currentStatusOfBaby: String? = null,
    val currentStatusOfBabyId: Int? = null,
    val causeOfDeath: String? = null,
    val otherCauseOfDeath: String? = null,
    val birthDoseVaccinesGiven: String? = null,
    val reasonForNoVaccines: String? = null,
    val vitaminKInjectionGiven: Boolean? = null,
    val reasonForNoVitaminK: String? = null,
    val birthCertificateIssued: String? = null,
    val birthCertificateIssuedId: Int? = null,
    val isStillbirth: Boolean? = null,
    val isNeonatalDeath: Boolean? = null,
    val createdDate: String? = null,
    val createdBy: String,
    val updatedDate: String? = null,
    val updatedBy: String
)
