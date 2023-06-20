package org.piramalswasthya.cho.model

data class PatientDetails(

    // personal information
    val firstName: String,
    val lastName: String,
    val contactNo: String,
    val gender: String,
    val age: Int,

    // visit details
    val serviceCategory: String,
    val subCategory: String,
    val reasonForVisit: String,

    // chief complaint
    val chiefComplaints: List<ChiefComplaint>,

    // vitals
    val weight: Float,
    val temperature: Float,
    val bloodPressure: Float,

    // prescription
    val form: String,
    val medicine: String,
    val dosage: String,
    val frequency: String,


)


data class ChiefComplaint(

    // chief complaint
    val chiefComplaint: String,
    val duration: Int,
    val unitOfDuration: String,

)
