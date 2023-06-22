package org.piramalswasthya.cho.ui.edit_patient_details_activity.visit_details

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentVisitDetailsBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsViewModel

class VisitDetailsFragment constructor(
    private val patientDetails: PatientDetails,
): Fragment() {

    private var _binding: FragmentVisitDetailsBinding? = null

    private val binding: FragmentVisitDetailsBinding
        get() = _binding!!

    private lateinit var viewModel: PersonalDetailsViewModel

    val duration = arrayOf("Hours","Minutes")

    val subcategories = arrayOf(
        "Management of Common Communicable Diseases",
        "Outpatient Care for Acute Simple Illnesses & Minor Ailments"
    )

    val reasons = arrayOf("New Chief Complaint(s)", "Follow up")

    val categories = arrayOf("General OPD Care")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitDetailsBinding.inflate(layoutInflater, container, false)
        setupSubcategoriesSpinner(binding.root)
        setupCategoriesSpinner(binding.root)
        setupReasonForVisitSpinner(binding.root)
        setupUnitSpinner(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChangedListener()
        setInitialValues()

    }

    fun setOnChangedListener(){

        binding.spCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.serviceCategory = categories[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.spSubcategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.subCategory = subcategories[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.spReasonForVisit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.reasonForVisit = reasons[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.etDuration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                patientDetails.duration = binding.etDuration.text.toString().toInt()
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.spUnitOfDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                patientDetails.unitOfDuration = duration[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    fun setInitialValues(){
        if(patientDetails.serviceCategory != null)
            binding.spCategories.setSelection(
                (binding.spCategories.adapter as ArrayAdapter<String?>).getPosition(
                    patientDetails.serviceCategory
                )
            )

        if(patientDetails.subCategory != null)
            binding.spSubcategories.setSelection(
                (binding.spSubcategories.adapter as ArrayAdapter<String>).getPosition(
                    patientDetails.subCategory
                )
            )

        if(patientDetails.reasonForVisit != null)
            binding.spReasonForVisit.setSelection(
                (binding.spReasonForVisit.adapter as ArrayAdapter<String>).getPosition(
                    patientDetails.reasonForVisit
                )
            )

        if(patientDetails.duration != null)
            binding.etDuration.setText(patientDetails.duration.toString())

        if(patientDetails.unitOfDuration != null)
            binding.spUnitOfDuration.setSelection(
                (binding.spUnitOfDuration.adapter as ArrayAdapter<String>).getPosition(
                    patientDetails.unitOfDuration
                )
            )

    }

    private fun setupSubcategoriesSpinner(view: View) {
        val subcategoriesSpinner = binding.spSubcategories
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subcategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subcategoriesSpinner.adapter = adapter
    }

    private fun setupCategoriesSpinner(view: View) {
        val categoriesSpinner: Spinner = view.findViewById(R.id.spCategories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = adapter
    }

    private fun setupReasonForVisitSpinner(view: View) {
        val reasonForVisitSpinner: Spinner = view.findViewById(R.id.spReasonForVisit)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reasons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reasonForVisitSpinner.adapter = adapter
    }

    private fun setupUnitSpinner(view: View) {
        val categoriesSpinner: Spinner = view.findViewById(R.id.spUnitOfDuration)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, duration)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = adapter
    }

}
