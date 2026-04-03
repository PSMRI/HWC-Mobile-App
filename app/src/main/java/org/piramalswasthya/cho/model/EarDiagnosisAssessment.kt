package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "EAR_DIAGNOSIS_ASSESSMENT")
@JsonClass(generateAdapter = true)
data class EarDiagnosisAssessment(


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assessment_id")
    val assessmentId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,


    @ColumnInfo(name = "difficulty_hearing")
    var difficultyHearing: Boolean? = null,

    @ColumnInfo(name = "whisper_test_response")
    var whisperTestResponse: String? = null,

    @ColumnInfo(name = "hearing_test_outcome")
    var hearingTestOutcome: String? = null,

    @ColumnInfo(name = "ear_pain")
    var earPain: Boolean? = null,

    @ColumnInfo(name = "ear_discharge_present")
    var earDischargePresent: Boolean? = null,

    @ColumnInfo(name = "foreign_body_in_ear")
    var foreignBodyInEar: String? = null,

    @ColumnInfo(name = "ear_condition_type")
    var earConditionType: String? = null,

    @ColumnInfo(name = "congenital_ear_malformation")
    var congenitalEarMalformation: Boolean? = null,

    @ColumnInfo(name = "syncState")
    var syncState: Int = 0

) : FormDataModel
