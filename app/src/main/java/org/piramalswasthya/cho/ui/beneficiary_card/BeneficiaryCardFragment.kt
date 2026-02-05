package org.piramalswasthya.cho.ui.beneficiary_card

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.piramalswasthya.cho.R
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.FragmentBeneficiaryCardBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity

@AndroidEntryPoint
class BeneficiaryCardFragment : Fragment() {

    private var _binding: FragmentBeneficiaryCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BeneficiaryCardViewModel by viewModels()

    private var patientInfo: PatientDisplayWithVisitInfo? = null
    private var statusOfWomanID: Int? = null

    companion object {
        private const val ARG_PATIENT_INFO = "patientInfo"

        fun newInstance(patientInfo: PatientDisplayWithVisitInfo): BeneficiaryCardFragment {
            return BeneficiaryCardFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PATIENT_INFO, patientInfo)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            patientInfo = it.getSerializable(ARG_PATIENT_INFO) as? PatientDisplayWithVisitInfo
            statusOfWomanID = it.getInt("statusOfWomanID", -1).takeIf { it != -1 }
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

        // Set patient info from arguments or activity intent
        patientInfo?.let {
            viewModel.setPatientInfo(it)
        } ?: run {
            // Try to get from activity intent
            @Suppress("DEPRECATION")
            val intentPatientInfo = activity?.intent?.getSerializableExtra(ARG_PATIENT_INFO) as? PatientDisplayWithVisitInfo
            intentPatientInfo?.let {
                viewModel.setPatientInfo(it)
                patientInfo = it
            }
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


        viewModel.navigateToContinue.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                onContinue()
                viewModel.onContinueNavigated()
            }
        }

        binding.btnGenerateAbha.setOnClickListener {
            navigateToAbhaGeneration()
        }

        binding.btnEdit.setOnClickListener {
            navigateToEditBeneficiaryDetails()
        }

        binding.btnContinue.setOnClickListener {
            onContinue()
        }

        // Handle updated patient info after returning from EditBeneficiaryDetailsFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<PatientDisplayWithVisitInfo>("updatedPatientInfo")?.observe(viewLifecycleOwner) { updatedInfo ->
            patientInfo = updatedInfo
            statusOfWomanID = updatedInfo.patient.statusOfWomanID
            viewModel.setPatientInfo(updatedInfo)
        }
    }

    private fun navigateToEditBeneficiaryDetails() {
        patientInfo?.let { patient ->
            val bundle = Bundle().apply {
                putSerializable("patientInfo", patient)
            }
            findNavController().navigate(R.id.action_beneficiaryCardFragment_to_editBeneficiaryDetailsFragment, bundle)
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
        patientInfo?.let { patient ->
            val intent = Intent(requireContext(), org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity::class.java)
            intent.putExtra("benVisitInfo", patient)

            val currentStatus = patient.patient.statusOfWomanID
            when (currentStatus) {
                1 -> intent.putExtra("navigateToEC", true)
                2 -> intent.putExtra("navigateToPW", true)
                3 -> intent.putExtra("navigateToPN", true)
            }

            startActivity(intent)
        }
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}