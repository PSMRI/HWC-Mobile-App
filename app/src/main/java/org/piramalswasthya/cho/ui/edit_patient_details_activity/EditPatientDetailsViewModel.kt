package org.piramalswasthya.cho.ui.edit_patient_details_activity

import android.util.Log
import androidx.lifecycle.ViewModel

class EditPatientDetailsViewModel : ViewModel() {
    data class PatientDetails(
        val firstName: String,
        val lastName: String,
        val contactNo: String,
        val gender: String,
        val age: String
    )

    private var patientDetails: PatientDetails? = null

    fun savePatientDetails(firstName: String, lastName: String, contactNo: String, gender: String, age: String) {
        patientDetails = PatientDetails(firstName, lastName, contactNo, gender, age)
        logPatientDetails()
    }

    fun getPatientDetails(): PatientDetails? {
        return patientDetails
    }

    private fun logPatientDetails() {
        patientDetails?.let {
            Log.d("PatientDetails", "First Name: ${it.firstName}")
            Log.d("PatientDetails", "Last Name: ${it.lastName}")
            Log.d("PatientDetails", "Contact No: ${it.contactNo}")
            Log.d("PatientDetails", "Gender: ${it.gender}")
            Log.d("PatientDetails", "Age: ${it.age}")
        }
    }

}
