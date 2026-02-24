package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "PAIN_SYMPTOM_ASSESSMENT")
@JsonClass(generateAdapter = true)
data class PainAndSymptomAssessment(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assessment_id")
    val assessmentId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,


    @ColumnInfo(name = "pain_severity")
    var painSeverity: String? = null,

    @ColumnInfo(name = "pain_duration")
    var painDuration: String? = null,


    @ColumnInfo(name = "symptoms_present")
    var symptomsPresent: Boolean? = null,

    @ColumnInfo(name = "other_symptoms_severity")
    var otherSymptomsSeverity: String? = null,

    // ---------------- Relief ----------------

    @ColumnInfo(name = "immediate_relief_provided")
    var immediateReliefProvided: Boolean? = null
) : FormDataModel