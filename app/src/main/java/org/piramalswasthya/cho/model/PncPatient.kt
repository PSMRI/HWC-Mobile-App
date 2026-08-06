package org.piramalswasthya.cho.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Simplified patient data class for PNC without delivery outcome
 */
data class PncPatientCache(
    @Embedded
    val patient: Patient,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val pncRecords: List<PNCVisitCache>
) {
    fun asDomainModel(): PncPatientDomain {
        return PncPatientDomain(
            patient = patient,
            latestPnc = pncRecords.maxByOrNull { it.pncPeriod },
            allPncRecords = pncRecords
        )
    }
}

/**
 * Simplified domain model for displaying patient with PNC data
 */
data class PncPatientDomain(
    val patient: Patient,
    val latestPnc: PNCVisitCache?,
    val allPncRecords: List<PNCVisitCache>
)
