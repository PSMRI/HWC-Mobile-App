package org.piramalswasthya.cho.ui.commons.personal_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.etFirstName.setText(patientDetails.firstName)
//        binding.etLastName.setText(patientDetails.lastName)
//        binding.etAge.setText(patientDetails.age)
//        binding.etContactNo.setText(patientDetails.contactNo)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PersonalDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}