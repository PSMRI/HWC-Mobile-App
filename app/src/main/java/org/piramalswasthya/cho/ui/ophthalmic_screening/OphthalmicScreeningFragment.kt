package org.piramalswasthya.cho.ui.ophthalmic_screening

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
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

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelAction()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_ophthalmic_screening, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.resetFields()
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupClickListeners()
        setupObservers()
        viewModel.loadOphthalmicVisit(args.patientID, args.benVisitNo, args.reasonForVisit)

        val headerTitle = if (args.reasonForVisit == DropdownConst.REASON_SYMPTOMATIC)
            getString(R.string.ophthalmic_symptomatic_title)
        else
            getString(R.string.ophthalmic_screening_title)
        activity?.findViewById<android.widget.TextView>(R.id.header_text_register_patient)?.text = headerTitle

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun setupDropdowns() {
        val chartValues = DropdownConst.visualAcuityChartList
        val chartAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, chartValues)
        binding.actvChartUsed.setAdapter(chartAdapter)
        binding.actvChartUsed.setOnItemClickListener { _, _, position, _ ->
            viewModel.setChartUsed(chartValues[position])
        }

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

        val nearVaValues = DropdownConst.nearVisualAcuityList
        val nearVaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nearVaValues)
        binding.actvNearVa.setAdapter(nearVaAdapter)
        binding.actvNearVa.setOnItemClickListener { _, _, position, _ ->
            viewModel.setNearVA(nearVaValues[position])
        }
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener { navigateAfterSave() }
    }

    private fun navigateAfterSave() {
        binding.btnNext.isEnabled = false
        viewModel.save {
            Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
            val benVisitInfo = args.benVisitInfo
            if (benVisitInfo != null) {
                findNavController().navigate(
                    OphthalmicScreeningFragmentDirections
                        .actionOphthalmicScreeningFragmentToFhirVisitDetailsFragment(benVisitInfo)
                )
            } else {
                binding.btnNext.isEnabled = true
                Toast.makeText(requireContext(), "Error returning to details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.showScreeningModule.observe(viewLifecycleOwner) { show ->
            binding.llScreeningModule.visibility = if (show) View.VISIBLE else View.GONE
        }

        viewModel.isDiabetic.observe(viewLifecycleOwner) { isDiabetic ->
            when (isDiabetic) {
                true  -> binding.rgIsDiabetic.check(R.id.rb_diabetic_yes)
                false -> binding.rgIsDiabetic.check(R.id.rb_diabetic_no)
                null  -> binding.rgIsDiabetic.clearCheck()
            }
            binding.llScreeningSection.visibility = if (isDiabetic == true) View.VISIBLE else View.GONE
        }

        viewModel.isScreeningPerformed.observe(viewLifecycleOwner) { performed ->
            when (performed) {
                true  -> binding.rgScreeningPerformed.check(R.id.rb_screening_yes)
                false -> binding.rgScreeningPerformed.check(R.id.rb_screening_no)
                null  -> binding.rgScreeningPerformed.clearCheck()
            }
        }

        viewModel.showChartSection.observe(viewLifecycleOwner) { show ->
            binding.llChartSection.visibility = if (show) View.VISIBLE else View.GONE
        }

        viewModel.showDistVASection.observe(viewLifecycleOwner) { show ->
            binding.llDistVaSection.visibility = if (show) View.VISIBLE else View.GONE
        }

        viewModel.showNearVASection.observe(viewLifecycleOwner) { show ->
            binding.llNearVaSection.visibility = if (show) View.VISIBLE else View.GONE
        }


        viewModel.saveError.observe(viewLifecycleOwner) { failed ->
            if (failed) {
                binding.btnNext.isEnabled = true
                Toast.makeText(requireContext(), "Save failed. Please retry.", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun getFragmentId(): Int = R.id.ophthalmicScreeningFragment

    override fun onSubmitAction() { navigateAfterSave() }

    override fun onCancelAction() { findNavController().popBackStack() }
}
