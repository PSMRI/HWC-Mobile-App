package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.database.room.SyncState

/**
 * Main record for Newborn Outcome - One record per delivery/mother
 */
@Entity(
    tableName = "NEWBORN_OUTCOME",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"),
        childColumns = arrayOf("motherPatientID"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "nbOutcomeInd", value = ["motherPatientID"])]
)
data class NewbornOutcomeCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val motherPatientID: String,
    var deliveryID: Long? = null, // Link to delivery outcome if exists
    var numberOfNeonates: Int, // 1, 2, 3, 4+
    var isActive: Boolean = true,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long,
    var updatedBy: String,
    var updatedDate: Long,
    var syncState: SyncState
) : FormDataModel

/**
 * Detailed record for each neonate - Multiple records per NewbornOutcome
 */
@Entity(
    tableName = "NEONATE_DETAILS",
    foreignKeys = [ForeignKey(
        entity = NewbornOutcomeCache::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("newbornOutcomeID"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "neonateInd", value = ["newbornOutcomeID", "neonateIndex"])]
)
data class NeonateDetailsCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val newbornOutcomeID: Long, // FK to NewbornOutcomeCache
    val neonateIndex: Int, // 0, 1, 2, 3 (for 1st, 2nd, 3rd, 4th baby)
    var neonateID: String, // Unique identifier for this neonate
    
    // Basic Outcome
    var outcomeAtBirth: String, // Live Birth, Stillbirth Fresh, Stillbirth Macerated, Died during delivery
    
    // Neonatal Details (enabled for Live Birth only)
    var sex: String? = null, // Male, Female, Ambiguous
    var criedImmediately: String? = null, // Immediate cry, Cried after resuscitation, Not applicable
    var resuscitationType: String? = null, // Multi-select: Stimulation, Suctioning, Bag and mask, etc.
    var birthWeight: Double? = null, // In grams (500-6000)
    
    // Congenital Anomalies
    var congenitalAnomalyDetected: String? = null, // Yes, No, Suspected
    var typeOfCongenitalAnomaly: String? = null, // Multi-select
    var otherCongenitalAnomaly: String? = null,
    
    // Complications & Status
    var newbornComplications: String? = null, // Multi-select
    var currentStatusOfBaby: String, // Healthy, Admitted SNCU, Admitted General, Died
    var causeOfDeath: String? = null, // Multi-select if died
    var otherCauseOfDeath: String? = null,
    
    // Immediate Newborn Care
    var birthDoseVaccines: String? = null, // Multi-select: BCG, Hep B, OPV-0
    var reasonForNoVaccines: String? = null,
    var vitaminKInjection: String? = null, // Yes, No
    var reasonForNoVitaminK: String? = null,
    var birthCertificateIssued: String? = null, // Yes, In process, No
    
    var isActive: Boolean = true,
    var createdBy: String,
    val createdDate: Long,
    var updatedBy: String,
    var updatedDate: Long,
    var syncState: SyncState
) : FormDataModel

// POST models for API
data class NewbornOutcomePost(
    val id: Long,
    val motherPatientID: String,
    val deliveryID: Long?,
    val numberOfNeonates: Int,
    val isActive: Boolean,
    val processed: String?,
    val createdBy: String,
    val createdDate: String?,
    val updatedBy: String,
    val updatedDate: String?
)

data class NeonateDetailsPost(
    val id: Long,
    val newbornOutcomeID: Long,
    val neonateIndex: Int,
    val neonateID: String,
    val outcomeAtBirth: String,
    val sex: String?,
    val criedImmediately: String?,
    val resuscitationType: String?,
    val birthWeight: Double?,
    val congenitalAnomalyDetected: String?,
    val typeOfCongenitalAnomaly: String?,
    val otherCongenitalAnomaly: String?,
    val newbornComplications: String?,
    val currentStatusOfBaby: String,
    val causeOfDeath: String?,
    val otherCauseOfDeath: String?,
    val birthDoseVaccines: String?,
    val reasonForNoVaccines: String?,
    val vitaminKInjection: String?,
    val reasonForNoVitaminK: String?,
    val birthCertificateIssued: String?,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String?,
    val updatedBy: String,
    val updatedDate: String?
)
