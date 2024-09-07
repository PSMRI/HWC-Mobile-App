package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class PatientVisitDataBundle(
    val patient : Patient,
    val patientVisitInfoSync : PatientVisitInfoSync,
    val visit : VisitDB,
    val chiefComplaints : List<ChiefComplaintDB>,
    val vitals : PatientVitalsModel
): Serializable

@JsonClass(generateAdapter = true)
data class PatientDoctorBundle(
    val patient: Patient,
    val patientVisitInfoSync: PatientVisitInfoSync,
    val prescriptionCaseRecordVal: List<PrescriptionWithItemMasterAndDrugFormMaster>?
): Serializable

data class PayloadWrapper(
    val type:String,
    val data:String
)