package org.piramalswasthya.cho.ui.edit_patient_details_activity.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addButton.setOnClickListener {
//logic to set fields
       }


        binding.submitButton.setOnClickListener {
            Toast.makeText(this.context,"Submitted ",Toast.LENGTH_SHORT).show()
        }
    }
}