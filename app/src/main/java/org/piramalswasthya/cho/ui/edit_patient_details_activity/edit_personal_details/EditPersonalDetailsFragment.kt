package org.piramalswasthya.cho.ui.edit_patient_details_activity.edit_personal_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentEditPersonalDetailsBinding
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsViewModel

class EditPersonalDetailsFragment constructor(
    private val patientDetails: PatientDetails,
): Fragment() {

    private var _binding: FragmentEditPersonalDetailsBinding? = null

    private val binding: FragmentEditPersonalDetailsBinding
        get() = _binding!!

    private lateinit var viewModel: PersonalDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPersonalDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPersonalDetails = PersonalDetailsFragment(patientDetails);
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.patientPersonalDetails.id, fragmentPersonalDetails)
        fragmentTransaction.commit()

        binding.btnSubmit.setOnClickListener {
            Log.i("tag", "going to visit details")
            findNavController().navigate(
                EditPersonalDetailsFragmentDirections.actionEditPersonalDetailsToVisitDetails(patientDetails)
            )
        }

//        binding.etFirstName.setText(patientDetails.firstName)
//        binding.etLastName.setText(patientDetails.lastName)
//        binding.etAge.setText(patientDetails.age)
//        binding.etContactNo.setText(patientDetails.contactNo)

    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(EditPersonalDetailsViewModel::class.java)
//        // TODO: Use the ViewModel
//    }

}