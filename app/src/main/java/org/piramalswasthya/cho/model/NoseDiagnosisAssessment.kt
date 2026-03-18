package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "NOSE_DIAGNOSIS_ASSESSMENT")
@JsonClass(generateAdapter = true)
data class NoseDiagnosisAssessment(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assessment_id")
    val assessmentId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,

    @ColumnInfo(name = "difficulty_breathing")
    var difficultyBreathing: Boolean? = null,

    @ColumnInfo(name = "open_mouth_breathing")
    var openMouthBreathing: Boolean? = null



) : FormDataModel
