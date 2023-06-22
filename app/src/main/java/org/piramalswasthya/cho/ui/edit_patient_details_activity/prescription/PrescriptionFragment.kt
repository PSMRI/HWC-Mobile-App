package org.piramalswasthya.cho.ui.edit_patient_details_activity.prescription

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOutreachBinding
import org.piramalswasthya.cho.databinding.FragmentPrescriptionBinding
import org.piramalswasthya.cho.model.PatientDetails

@AndroidEntryPoint
class PrescriptionFragment constructor(
    private val patientDetails: PatientDetails,
): Fragment() {

    private var _binding: FragmentPrescriptionBinding? = null

    private val binding: FragmentPrescriptionBinding
        get() = _binding!!

    private var formOptions : Array<String> = emptyArray()

    private var dosageOptions : Array<String> = emptyArray()

    private var frequencyOptions : Array<String> = emptyArray()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionBinding.inflate(layoutInflater, container, false)

//        val prescriptionLayout  = _binding!!.prescriptionDetailsCard
//        val addBtn = _binding!!.addButton
//        addBtn.setOnClickListener{
//            if(_binding!!.medicineEditText.text.toString()!="")
//                _binding!!.medValue.text =  _binding!!.medicineEditText.text.toString()
//
//            _binding!!.formValue.text = _binding!!.formSpinner.selectedItem.toString()
//            _binding!!.freqValue.text = _binding!!.frequencySpinner.selectedItem.toString()
//            _binding!!.doseValue.text = _binding!!.dosageSpinner.selectedItem.toString()
//
//
//            if (prescriptionLayout.visibility == View.GONE) {
//                prescriptionLayout.visibility = View.VISIBLE
//            }
//        }

        // Create a new array with the default text as the first item


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStrings()
        setupSpinners()
        setupOnChangeListners()
        setupInitialValues()

        binding.addButton.setOnClickListener{
            if(_binding!!.medicineEditText.text.toString()!="")
                _binding!!.medValue.text =  _binding!!.medicineEditText.text.toString()

            _binding!!.formValue.text = _binding!!.formSpinner.selectedItem.toString()
            _binding!!.freqValue.text = _binding!!.frequencySpinner.selectedItem.toString()
            _binding!!.doseValue.text = _binding!!.dosageSpinner.selectedItem.toString()


            if (binding.prescriptionDetailsCard.visibility == View.GONE) {
                binding.prescriptionDetailsCard.visibility = View.VISIBLE
            }
        }

    }

    private fun setupOnChangeListners(){

        binding.frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.frequency = frequencyOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.dosageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.dosage = dosageOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.formSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.form = formOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.medicineEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                patientDetails.medicine = binding.medicineEditText.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun setupInitialValues(){

        if(patientDetails.form != null)
            binding.formSpinner.setSelection(
                (binding.formSpinner.adapter as ArrayAdapter<String?>).getPosition(
                    patientDetails.form
                )
            )

        if(patientDetails.dosage != null)
            binding.dosageSpinner.setSelection(
                (binding.dosageSpinner.adapter as ArrayAdapter<String>).getPosition(
                    patientDetails.dosage
                )
            )

        if(patientDetails.frequency != null)
            binding.frequencySpinner.setSelection(
                (binding.frequencySpinner.adapter as ArrayAdapter<String>).getPosition(
                    patientDetails.frequency
                )
            )

        if(patientDetails.medicine != null)
            binding.medicineEditText.setText(patientDetails.medicine.toString())

    }

    private fun setupStrings(){
        formOptions = resources.getStringArray(R.array.form_options)
        dosageOptions = resources.getStringArray(R.array.dosage_options)
        frequencyOptions = resources.getStringArray(R.array.frequency_options)
    }

    private fun setupSpinners(){
        setupFormSpinner()
        setupDosageSpinner()
        setupFrequencySpinner()
    }

    private fun setupFormSpinner(){
        val formSpinner = binding.formSpinner
        val formSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, formOptions)
        formSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        formSpinner.adapter = formSpinnerAdapter
    }

    private fun setupDosageSpinner(){
        val dosageSpinner = binding.dosageSpinner
        val doseSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dosageOptions)
        doseSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dosageSpinner.adapter = doseSpinnerAdapter
    }

    private fun setupFrequencySpinner(){
        val frequencySpinner = binding.frequencySpinner
        val frequencySpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencyOptions)
        frequencySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frequencySpinner.adapter = frequencySpinnerAdapter
    }

}