package org.piramalswasthya.cho.ui.edit_patient_details_activity.visit_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
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

//        binding.spCategories.onItemSelectedListener()
//
//        if(patientDetails.firstName != null)
//            binding.etFirstName.setText(patientDetails.firstName)
//
//        if(patientDetails.lastName != null)
//            binding.etLastName.setText(patientDetails.lastName)
//
//        if(patientDetails.age != null)
//            binding.etAge.setText(patientDetails.age.toString())
//
//        if(patientDetails.contactNo != null)
//            binding.etContactNo.setText(patientDetails.contactNo)

        setVariableValues()

    }

    fun setVariableValues(){

        binding.spCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }

        }


//        binding.spCategories.setOnItemSelectedListener(AdapterView.OnItemSelectedListener {
//            @Override
//            public void onItemSelected(
//                AdapterView<?> parentView,
//                View selectedItemView,
//                int position,
//                long id
//            ) {
//                // your code here
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//                // your code here
//            }
//
//        });
    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_visit_details, container, false)
//
//        setupSubcategoriesSpinner(view)
//        setupCategoriesSpinner(view)
//        setupReasonForVisitSpinner(view)
//        setupUnitSpinner(view)
//        return view
//    }

    private fun setupSubcategoriesSpinner(view: View) {
        val subcategoriesSpinner: Spinner = view.findViewById(R.id.spSubcategories)
        val subcategories = arrayOf(
            "Management of Common Communicable Diseases",
            "Outpatient Care for Acute Simple Illnesses & Minor Ailments"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subcategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subcategoriesSpinner.adapter = adapter
    }

    private fun setupCategoriesSpinner(view: View) {
        val categoriesSpinner: Spinner = view.findViewById(R.id.spCategories)
        val categories = arrayOf("General OPD Care")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = adapter
    }

    private fun setupReasonForVisitSpinner(view: View) {
        val reasonForVisitSpinner: Spinner = view.findViewById(R.id.spReasonForVisit)
        val reasons = arrayOf("New Chief Complaint(s)", "Follow up")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reasons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reasonForVisitSpinner.adapter = adapter

        reasonForVisitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedReason = reasons[position]
                Toast.makeText(requireContext(), "Selected reason: $selectedReason", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    private fun setupUnitSpinner(view: View) {
        val categoriesSpinner: Spinner = view.findViewById(R.id.spUnitOfDuration)
        val categories = arrayOf("Hours","Minutes")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = adapter
    }
}
