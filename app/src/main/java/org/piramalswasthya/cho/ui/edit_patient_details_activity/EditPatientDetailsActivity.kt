package org.piramalswasthya.cho.ui.edit_patient_details_activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.edit_personal_details.EditPersonalDetailsFragment


@AndroidEntryPoint
class EditPatientDetailsActivity: AppCompatActivity() {

    private lateinit var viewModel: EditPatientDetailsViewModel

    private val currFragment: Int = R.id.fragment_personal_details;

    private var _binding : ActivityEditPatientDetailsBinding? = null

    private val binding  : ActivityEditPatientDetailsBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gson = Gson()
        val patientDetails: PatientDetails = gson.fromJson(intent.getStringExtra("patientDetails"), PatientDetails::class.java)

        viewModel = ViewModelProvider(this).get(EditPatientDetailsViewModel::class.java)

        val fragmentPersonalDetails = EditPersonalDetailsFragment(patientDetails);
        supportFragmentManager.beginTransaction().replace(binding.patientDetails.id, fragmentPersonalDetails).commit()

//        // Populate the spinner with options
//        genderSpinner()
//
//
//
//        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
//        btnSubmit.setOnClickListener {
//            // Call the method to retrieve and save the data
//            getFormData()
//        }
    }
    private fun genderSpinner(){
        val genderSpinner: Spinner = findViewById(R.id.spGender)
        val genderOptions = listOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter

        // Handle the user selection
        genderSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGender = genderOptions[position]
                if (selectedGender != "Select Gender") {
                    // Gender is selected, proceed with the chosen value
                } else {
                    // Prompt user to select a gender
                    Toast.makeText(this@EditPatientDetailsActivity, "Please select a gender", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case when nothing is selected
            }
        })
    }
    private fun getFormData() {
        val firstName = findViewById<EditText>(R.id.etFirstName).text.toString()
        val lastName = findViewById<EditText>(R.id.etLastName).text.toString()
        val contactNo = findViewById<EditText>(R.id.etContactNo).text.toString()
        val gender = findViewById<Spinner>(R.id.spGender).selectedItem.toString()
        val age = findViewById<EditText>(R.id.etAge).text.toString()

        viewModel.savePatientDetails(firstName, lastName, contactNo, gender, age)

        // Display a toast message to indicate successful data submission

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
