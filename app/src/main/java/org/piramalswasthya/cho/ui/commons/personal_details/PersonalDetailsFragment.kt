package org.piramalswasthya.cho.ui.commons.personal_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOutreachBinding
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.model.PatientDetails

class PersonalDetailsFragment constructor(
    private val patientDetails: PatientDetails,
): Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null

    private val binding: FragmentPersonalDetailsBinding
        get() = _binding!!

    private lateinit var viewModel: PersonalDetailsViewModel

    val genders = arrayOf("Male", "Female", "Transgender")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDetailsBinding.inflate(layoutInflater, container, false)
        setupGenderSpinner(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChangeListener()
        setInitialValues()
    }

    private fun setupGenderSpinner(view: View) {
        val subGenderSpinner = binding.spGender
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subGenderSpinner.adapter = adapter
    }

    private fun setInitialValues(){
        if(patientDetails.firstName != null)
            binding.etFirstName.setText(patientDetails.firstName)

        if(patientDetails.lastName != null)
            binding.etLastName.setText(patientDetails.lastName)

        if(patientDetails.age != null)
            binding.etAge.setText(patientDetails.age.toString())

        if(patientDetails.contactNo != null)
            binding.etContactNo.setText(patientDetails.contactNo)

        if(patientDetails.gender != null)
            binding.spGender.setSelection(
                (binding.spGender.adapter as ArrayAdapter<String>).getPosition(
                    patientDetails.gender
                )
            )
    }

    private fun setOnChangeListener(){

        binding.etFirstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                patientDetails.firstName = binding.etFirstName.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etLastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                patientDetails.lastName = binding.etLastName.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etContactNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                patientDetails.contactNo = binding.etContactNo.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etAge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                patientDetails.age = binding.etAge.text.toString().toInt()
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.spGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.gender = genders[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PersonalDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}