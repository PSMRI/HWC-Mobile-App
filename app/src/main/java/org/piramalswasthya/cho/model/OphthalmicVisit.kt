package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.utils.generateUuid
import java.io.Serializable

@Entity(
    tableName = "OPHTHALMIC_VISIT",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientID"],
            childColumns = ["patientID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@JsonClass(generateAdapter = true)
data class OphthalmicVisit(
    @PrimaryKey
    val visitId: String = generateUuid(),
    
    @ColumnInfo(name = "patientID")
    val patientID: String,
    
    @ColumnInfo(name = "benVisitNo")
    val benVisitNo: Int,
    
    // Ticket 1: Diabetic Screening
    @ColumnInfo(name = "isDiabetic")
    var isDiabetic: Boolean? = null,
    
    @ColumnInfo(name = "screeningPerformed")
    var screeningPerformed: Boolean? = null,
    
    @ColumnInfo(name = "visualAcuityChartUsed")
    var visualAcuityChartUsed: String? = null,
    
    @ColumnInfo(name = "distVARight")
    var distVARight: String? = null,
    
    @ColumnInfo(name = "distVALeft")
    var distVALeft: String? = null,
    
    @ColumnInfo(name = "nearVA")
    var nearVA: String? = null,
    
    // Ticket 4 & 5: Case Identification
    @ColumnInfo(name = "caseIdConditions")
    var caseIdConditions: String? = null, // JSON list of conditions
    
    // Ticket 6: Symptoms
    @ColumnInfo(name = "cataractSymptoms")
    var cataractSymptoms: Boolean? = null,
    
    @ColumnInfo(name = "glaucomaSymptoms")
    var glaucomaSymptoms: Boolean? = null,
    
    @ColumnInfo(name = "diabeticRetinopathySymptoms")
    var diabeticRetinopathySymptoms: Boolean? = null,
    
    @ColumnInfo(name = "presbyopiaSymptoms")
    var presbyopiaSymptoms: Boolean? = null,
    
    @ColumnInfo(name = "trachomaStatus")
    var trachomaStatus: String? = null,
    
    @ColumnInfo(name = "cornealDiseaseType")
    var cornealDiseaseType: String? = null,
    
    @ColumnInfo(name = "vitaminADeficiency")
    var vitaminADeficiency: Boolean? = null,
    
    // Ticket 8 & 9: Injury and Trauma
    @ColumnInfo(name = "injuryType")
    var injuryType: String? = null, // JSON list of injury types
    
    @ColumnInfo(name = "foreignBodyRemoval")
    var foreignBodyRemoval: String? = null,
    
    @ColumnInfo(name = "chemicalExposure")
    var chemicalExposure: Boolean? = null,
    
    // Audit Fields
    @ColumnInfo(name = "createdBy")
    var createdBy: String,
    
    @ColumnInfo(name = "createdDate")
    var createdDate: Long,
    
    @ColumnInfo(name = "updatedBy")
    var updatedBy: String,
    
    @ColumnInfo(name = "updatedDate")
    var updatedDate: Long,
    
    @ColumnInfo(name = "syncState")
    var syncState: Int
) : Serializable
