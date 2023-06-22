package org.piramalswasthya.cho.model

import android.os.Parcel
import android.os.Parcelable

class PatientDetails constructor() {
    // personal information
    var firstName: String? = null
    var lastName: String? = null
    var contactNo: String? = null
    var gender: String? = null
    var age: Int? = null

    // visit details
    var serviceCategory: String? = null
    var subCategory: String? = null
    var reasonForVisit: String? = null

    // chief complaint
    var chiefComplaints: List<ChiefComplaint> = emptyList()

    // vitals
    var weight: Float? = null
    var temperature: Float? = null
    var bloodPressure: Float? = null

    // prescription
    var form: String? = null
    var medicine: String? = null
    var dosage: String? = null
    var frequency: String? = null

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