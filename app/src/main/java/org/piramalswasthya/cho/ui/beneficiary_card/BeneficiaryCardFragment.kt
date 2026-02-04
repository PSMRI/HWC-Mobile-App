package org.piramalswasthya.cho.ui.beneficiary_card

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.FragmentBeneficiaryCardBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.beneficiary_card.edit.EditBeneficiaryDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity

@AndroidEntryPoint
class BeneficiaryCardFragment : Fragment() {

    private var _binding: FragmentBeneficiaryCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BeneficiaryCardViewModel by viewModels()

    private var patientInfo: PatientDisplayWithVisitInfo? = null
    private var statusOfWomanID: Int? = null

    companion object {
        private const val ARG_PATIENT_INFO = "patientInfo"
        private const val ARG_STATUS_OF_WOMAN_ID = "statusOfWomanID"

        fun newInstance(patientInfo: PatientDisplayWithVisitInfo, statusOfWomanID: Int? = null): BeneficiaryCardFragment {
            return BeneficiaryCardFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PATIENT_INFO, patientInfo)
                    statusOfWomanID?.let { putInt(ARG_STATUS_OF_WOMAN_ID, it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            patientInfo = it.getSerializable(ARG_PATIENT_INFO) as? PatientDisplayWithVisitInfo
            statusOfWomanID = it.getInt(ARG_STATUS_OF_WOMAN_ID, -1).takeIf { id -> id != -1 }
        }
        
        // Try to get from activity intent if not in arguments
        if (patientInfo == null) {
            @Suppress("DEPRECATION")
            patientInfo = activity?.intent?.getSerializableExtra(ARG_PATIENT_INFO) as? PatientDisplayWithVisitInfo
            statusOfWomanID = activity?.intent?.getIntExtra(ARG_STATUS_OF_WOMAN_ID, -1)?.takeIf { it != -1 }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeneficiaryCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set patient info
        patientInfo?.let {
            viewModel.setPatientInfo(it)
        }

        // Bind data
        viewModel.patientInfo.observe(viewLifecycleOwner) { patient ->
            patient?.let {
                binding.patient = it
                binding.viewModel = viewModel
                binding.executePendingBindings()
            }
        }

        // Navigate to ABHA generation
        viewModel.navigateToAbha.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                navigateToAbhaGeneration()
                viewModel.onAbhaNavigated()
            }
        }

        // Continue navigation
        viewModel.navigateToContinue.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                onContinue()
                viewModel.onContinueNavigated()
            }
        }

        // Handle Edit button click
        binding.btnEdit.setOnClickListener {
            navigateToEditBeneficiaryDetails()
        }
    }

    private fun navigateToEditBeneficiaryDetails() {
        patientInfo?.let { patient ->
            val fragment = EditBeneficiaryDetailsFragment.newInstance(patient)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun navigateToAbhaGeneration() {
        patientInfo?.let { patient ->
            val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("benId", patient.patient.beneficiaryID)
            intent.putExtra("benRegId", patient.patient.beneficiaryRegID)
            startActivity(intent)
        }
    }

    private fun onContinue() {
        // Navigate to the next screen based on Status of Woman
        navigateToNextScreen()
    }
    
    private fun navigateToNextScreen() {
        patientInfo?.let { patient ->
            val intent = Intent(requireContext(), EditPatientDetailsActivity::class.java)
            intent.putExtra("benVisitInfo", patient)

            when (statusOfWomanID) {
                1 -> {
                    // EC - Navigate to Eligible Couple Tracking
                    intent.putExtra("navigateToEC", true)
                }
                2 -> {
                    // PW - Navigate to ANC/Pregnancy Module
                    intent.putExtra("navigateToPW", true)
                }
                3 -> {
                    // Postnatal - Navigate to PNC Module
                    intent.putExtra("navigateToPN", true)
                }
            }

            startActivity(intent)
        }
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
