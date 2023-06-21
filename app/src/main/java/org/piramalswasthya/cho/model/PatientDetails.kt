package org.piramalswasthya.cho.model

import android.os.Parcel
import android.os.Parcelable

class PatientDetails constructor() {
    // personal information
    val firstName: String = ""
    val lastName: String = ""
    val contactNo: String = ""
    val gender: String = ""
    val age: Int = 0

    // visit details
    val serviceCategory: String = ""
    val subCategory: String = ""
    val reasonForVisit: String = ""

    // chief complaint
    val chiefComplaints: List<ChiefComplaint> = emptyList()

    // vitals
    val weight: Float = 0.0F
    val temperature: Float = 0.0F
    val bloodPressure: Float = 0.0F

    // prescription
    val form: String = ""
    val medicine: String = ""
    val dosage: String = ""
    val frequency: String = ""

//    constructor(parcel: Parcel) : this() {
//
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<PatientDetails> {
//        override fun createFromParcel(parcel: Parcel): PatientDetails {
//            return PatientDetails(parcel)
//        }
//
//        override fun newArray(size: Int): Array<PatientDetails?> {
//            return arrayOfNulls(size)
//        }
//    }
}