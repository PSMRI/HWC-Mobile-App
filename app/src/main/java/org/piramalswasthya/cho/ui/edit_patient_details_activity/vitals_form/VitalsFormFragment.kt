package org.piramalswasthya.cho.ui.edit_patient_details_activity.vitals_form
//
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentPrescriptionBinding
import org.piramalswasthya.cho.databinding.FragmentVitalsFormBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.edit_patient_details_activity.prescription.PrescriptionFragment

class VitalsFormFragment constructor(
    private val patientDetails: PatientDetails,
): Fragment() {

    private var _binding: FragmentVitalsFormBinding? = null

    private val binding: FragmentVitalsFormBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVitalsFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChangeListener()
        setInitialValues()
    }

    private fun setInitialValues(){
        if(patientDetails.weight != null)
            binding.weightEditText.setText(patientDetails.weight.toString())

        if(patientDetails.temperature != null)
            binding.temperatureEditText.setText(patientDetails.temperature.toString())

        if(patientDetails.bpSystolic != null)
            binding.bpSystolicEditText.setText(patientDetails.bpSystolic.toString())

        if(patientDetails.bpDiastolic != null)
            binding.bpDiastolicEditText.setText(patientDetails.bpDiastolic.toString())
    }

    private fun setOnChangeListener(){

        binding.weightEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = binding.weightEditText.text.toString();
                if(text.isEmpty()){
                    patientDetails.weight = null
                }
                else{
                    patientDetails.weight = text.toInt()
                }
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.temperatureEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = binding.temperatureEditText.text.toString();
                if(text.isEmpty()){
                    patientDetails.temperature = null
                }
                else{
                    patientDetails.temperature = text.toInt()
                }
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.bpDiastolicEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = binding.bpDiastolicEditText.text.toString();
                if(text.isEmpty()){
                    patientDetails.bpDiastolic = null
                }
                else{
                    patientDetails.bpDiastolic = text.toInt()
                }
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.bpSystolicEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = binding.bpSystolicEditText.text.toString();
                if(text.isEmpty()){
                    patientDetails.bpSystolic = null
                }
                else{
                    patientDetails.bpSystolic = text.toInt()
                }
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

}