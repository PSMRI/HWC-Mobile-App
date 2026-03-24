package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "THROAT_DIAGNOSIS_ASSESSMENT")
@JsonClass(generateAdapter = true)
data class ThroatDiagnosisAssessment(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assessment_id")
    val assessmentId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,

    /* -------------------- THROAT DIAGNOSIS FIELDS -------------------- */

    @ColumnInfo(name = "symptoms")
    var symptoms: List<String>? = null,

    @ColumnInfo(name = "neck_swelling")
    var neckSwelling: Boolean? = null,

    @ColumnInfo(name = "difficulty_swallowing")
    var difficultySwallowing: Boolean? = null,

    @ColumnInfo(name = "tonsillitis")
    var tonsillitis: Boolean? = null,

    @ColumnInfo(name = "pharyngitis")
    var pharyngitis: Boolean? = null,

    @ColumnInfo(name = "laryngitis")
    var laryngitis: Boolean? = null,

    @ColumnInfo(name = "sinusitis")
    var sinusitis: Boolean? = null,

    @ColumnInfo(name = "cleft_lip")
    var cleftLip: Boolean? = null,

    @ColumnInfo(name = "cleft_palate")
    var cleftPalate: Boolean? = null

) : FormDataModel