package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "PATIENT_VITALS")
@JsonClass(generateAdapter = true)
data class PatientVitalsModel (
    @PrimaryKey
    val vitalsId: String,
    @ColumnInfo(name = "height") val height: String?,
    @ColumnInfo(name = "weight") val weight: String?,
    @ColumnInfo(name = "bmi") val bmi: String?,
    @ColumnInfo(name = "waist_circumference") val waistCircumference: String?,
    @ColumnInfo(name = "temperature") val temperature: String?,
    @ColumnInfo(name = "pulse_rate") val pulseRate : String?,
    @ColumnInfo(name = "spo2") val spo2 : String?,
    @ColumnInfo(name = "bp_systolic") val bpSystolic : String?,
    @ColumnInfo(name = "bp_diastolic") val bpDiastolic : String?,
    @ColumnInfo(name = "respiratory_rate") val respiratoryRate : String?,
    @ColumnInfo(name = "rbs") val rbs: String?
)