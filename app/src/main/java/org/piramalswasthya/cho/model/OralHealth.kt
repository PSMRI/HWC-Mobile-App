package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(
    tableName = "ORAL_HEALTH",
    indices = [
        Index(value = ["patient_id"], name = "index_oral_health_patient_id"),
        Index(value = ["patient_id", "ben_visit_no"], name = "index_oral_health_patient_visit", unique = true)
    ]
)
@JsonClass(generateAdapter = true)
data class OralHealth(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "oral_health_id")
    val oralHealthId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,


    @ColumnInfo(name = "tooth_decay_present")
    var toothDecayPresent: Boolean? = null,

    @ColumnInfo(name = "tooth_decay_symptoms")
    var toothDecaySymptoms: String? = null,


    @ColumnInfo(name = "gum_disease_present")
    var gumDiseasePresent: Boolean? = null,

    @ColumnInfo(name = "gum_disease_symptoms")
    var gumDiseaseSymptoms: String? = null,


    @ColumnInfo(name = "irregular_teeth_jaws")
    var irregularTeethJaws: Boolean? = null,

    @ColumnInfo(name = "abnormal_growth_ulcer")
    var abnormalGrowthUlcer: Boolean? = null,

    @ColumnInfo(name = "cleft_lip_palate")
    var cleftLipPalate: Boolean? = null,

    @ColumnInfo(name = "dental_fluorosis")
    var dentalFluorosis: Boolean? = null,

    @ColumnInfo(name = "dental_emergency")
    var dentalEmergency: String? = null,

    @ColumnInfo(name = "created_date")
    var createdDate: Long? = null,

    @ColumnInfo(name = "created_by")
    var createdBy: String? = null,

    @ColumnInfo(name = "syncState")
    var syncState: Int = 0

) : FormDataModel

