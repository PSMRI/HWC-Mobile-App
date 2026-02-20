package org.piramalswasthya.cho.ui.ophthalmic_screening

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOphthalmicScreeningBinding
import org.piramalswasthya.cho.ui.commons.DropdownConst
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class OphthalmicScreeningFragment : Fragment(), NavigationAdapter {

    private val viewModel: OphthalmicScreeningViewModel by viewModels()
    private lateinit var binding: FragmentOphthalmicScreeningBinding
    private val args: OphthalmicScreeningFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_ophthalmic_screening, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        setupDropdowns()
        setupClickListeners()
        setupObservers()

        val patientID = args.patientID // Assuming non-nullable in nav graph
        val benVisitNo = args.benVisitNo
        if (patientID != null) {
            viewModel.loadOphthalmicVisit(patientID, benVisitNo)
        }
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelAction()
            }
        }
    }

    private fun setupDropdowns() {
        val vaValues = DropdownConst.visualAcuityList
        val vaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vaValues)

        binding.actvDistVaRight.setAdapter(vaAdapter)
        binding.actvDistVaLeft.setAdapter(vaAdapter)

        binding.actvDistVaRight.setOnItemClickListener { _, _, position, _ ->
            viewModel.setDistVARight(vaValues[position])
        }

        binding.actvDistVaLeft.setOnItemClickListener { _, _, position, _ ->
            viewModel.setDistVALeft(vaValues[position])
        }

        // Visual Acuity Chart Used dropdown
        val chartValues = DropdownConst.visualAcuityChartList
        val chartAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, chartValues)
        binding.actvVaChart.setAdapter(chartAdapter)
        binding.actvVaChart.setOnItemClickListener { _, _, position, _ ->
            viewModel.setVisualAcuityChart(chartValues[position])
            // Clear displayed text for downstream dropdowns
            binding.actvDistVaRight.setText("", false)
            binding.actvDistVaLeft.setText("", false)
            binding.actvNearVa.setText("", false)
        }

        // Near Visual Acuity dropdown
        val nearValues = DropdownConst.nearVisualAcuityList
        val nearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nearValues)
        binding.actvNearVa.setAdapter(nearAdapter)
        binding.actvNearVa.setOnItemClickListener { _, _, position, _ ->
            viewModel.setNearVA(nearValues[position])
        }
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            viewModel.save {
                Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
                val benVisitInfo = args.benVisitInfo
                if (benVisitInfo != null) {
                    findNavController().navigate(
                        OphthalmicScreeningFragmentDirections.actionOphthalmicScreeningFragmentToFhirVisitDetailsFragment(
                            benVisitInfo
                        )
                    )
                } else {
                    Toast.makeText(requireContext(), "Error returning to details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private var previousDiabeticState: Boolean? = null
    private var isDiabeticInitialized = false

    private fun setupObservers() {
        viewModel.isDiabetic.observe(viewLifecycleOwner) { isDiabetic ->
            when (isDiabetic) {
                null -> {
                    binding.rgIsDiabetic.clearCheck()
                    binding.llScreeningSection.visibility = View.GONE
                    binding.llVaChartSection.visibility = View.GONE
                }
                true -> {
                    binding.rgIsDiabetic.check(R.id.rb_diabetic_yes)
                    binding.llScreeningSection.visibility = View.VISIBLE
                    binding.llVaChartSection.visibility = View.GONE
                }
                false -> {
                    binding.rgIsDiabetic.check(R.id.rb_diabetic_no)
                    binding.llScreeningSection.visibility = View.GONE
                    binding.llVaChartSection.visibility = View.VISIBLE
                }
            }
            // Only clear dropdown text when transitioning from a previous state,
            // not on the initial emission which would overwrite data-binding values.
            if (isDiabeticInitialized && previousDiabeticState != isDiabetic) {
                binding.actvVaChart.setText("", false)
                binding.actvDistVaRight.setText("", false)
                binding.actvDistVaLeft.setText("", false)
                binding.actvNearVa.setText("", false)
            }
            previousDiabeticState = isDiabetic
            isDiabeticInitialized = true
        }
    }

    // --- NavigationAdapter ---

    override fun getFragmentId(): Int = R.id.ophthalmicScreeningFragment

    override fun onSubmitAction() {
        viewModel.save {
            Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
            val benVisitInfo = args.benVisitInfo
            if (benVisitInfo != null) {
                findNavController().navigate(
                    OphthalmicScreeningFragmentDirections.actionOphthalmicScreeningFragmentToFhirVisitDetailsFragment(
                        benVisitInfo
                    )
                )
            } else {
                Toast.makeText(requireContext(), "Error returning to details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCancelAction() {
        findNavController().popBackStack()
    }
}
