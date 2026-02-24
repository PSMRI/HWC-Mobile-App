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
import android.app.AlertDialog
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
        observeConditionSubFields()
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

        val trachomaValues = DropdownConst.trachomaStatusList
        val trachomaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, trachomaValues)
        binding.actvTrachomaStatus.setAdapter(trachomaAdapter)
        binding.actvTrachomaStatus.setOnItemClickListener { _, _, position, _ ->
            viewModel.setTrachomaStatus(trachomaValues[position])
        }

        val cornealValues = DropdownConst.cornealDiseaseTypeList
        val cornealAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cornealValues)
        binding.actvCornealDiseaseType.setAdapter(cornealAdapter)
        binding.actvCornealDiseaseType.setOnItemClickListener { _, _, position, _ ->
            viewModel.setCornealDiseaseType(cornealValues[position])
        }
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener { navigateAfterSave() }

        binding.tvCaseIdSelection.setOnClickListener {
            showCaseIdMultiSelectDialog()
        }

        binding.rgCataractSymptoms.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_cataract_yes -> viewModel.setCataractSymptoms(true)
                R.id.rb_cataract_no -> viewModel.setCataractSymptoms(false)
            }
        }

        binding.rgGlaucomaSymptoms.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_glaucoma_yes -> viewModel.setGlaucomaSymptoms(true)
                R.id.rb_glaucoma_no -> viewModel.setGlaucomaSymptoms(false)
            }
        }

        binding.rgDrSymptoms.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_dr_yes -> viewModel.setDrSymptoms(true)
                R.id.rb_dr_no -> viewModel.setDrSymptoms(false)
            }
        }

        binding.rgPresbyopiaSymptoms.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_presbyopia_yes -> viewModel.setPresbyopiaSymptoms(true)
                R.id.rb_presbyopia_no -> viewModel.setPresbyopiaSymptoms(false)
            }
        }

        binding.rgVitaminADeficiency.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_vitamin_a_yes -> viewModel.setVitaminADeficiency(true)
                R.id.rb_vitamin_a_no -> viewModel.setVitaminADeficiency(false)
            }
        }
    }

    private fun showCaseIdMultiSelectDialog() {
        val options = DropdownConst.caseIdConditionsList.toTypedArray()
        val currentSelections = viewModel.caseIdConditions.value ?: emptyList()
        val checkedItems = BooleanArray(options.size) { index ->
            currentSelections.contains(options[index])
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.ophthalmic_case_id_conditions))
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                val selectedList = mutableListOf<String>()
                for (i in options.indices) {
                    if (checkedItems[i]) {
                        selectedList.add(options[i])
                    }
                }
                viewModel.setCaseIdConditions(selectedList)
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
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
            applyIsDiabeticState(isDiabetic)
        }

        viewModel.isScreeningPerformed.observe(viewLifecycleOwner) { performed ->
            applyScreeningPerformedState(performed)
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

        viewModel.caseIdConditions.observe(viewLifecycleOwner) { conditions ->
            if (conditions.isNullOrEmpty()) {
                binding.tvCaseIdSelection.setText("")
                binding.tvCaseIdSelection.hint = getString(R.string.select_conditions)
            } else {
                binding.tvCaseIdSelection.setText(conditions.joinToString(", "))
            }
        }
    }

    private fun observeConditionSubFields() {
        observeSubFieldVisibility()
        observeSubFieldAlerts()
        observeSubFieldRestoration()
    }

    private fun observeSubFieldVisibility() {
        viewModel.showCataractSubField.observe(viewLifecycleOwner) { show ->
            binding.llCataractSubfield.visibility = if (show) View.VISIBLE else View.GONE
            if (!show) binding.rgCataractSymptoms.clearCheck()
        }
        viewModel.showGlaucomaSubField.observe(viewLifecycleOwner) { show ->
            binding.llGlaucomaSubfield.visibility = if (show) View.VISIBLE else View.GONE
            if (!show) binding.rgGlaucomaSymptoms.clearCheck()
        }
        viewModel.showDrSubField.observe(viewLifecycleOwner) { show ->
            binding.llDrSubfield.visibility = if (show) View.VISIBLE else View.GONE
            if (!show) binding.rgDrSymptoms.clearCheck()
        }
        viewModel.showPresbyopiaSubField.observe(viewLifecycleOwner) { show ->
            binding.llPresbyopiaSubfield.visibility = if (show) View.VISIBLE else View.GONE
            if (!show) binding.rgPresbyopiaSymptoms.clearCheck()
        }
        viewModel.showTrachomaSubField.observe(viewLifecycleOwner) { show ->
            binding.llTrachomaSubfield.visibility = if (show) View.VISIBLE else View.GONE
            if (!show) binding.actvTrachomaStatus.setText("")
        }
        viewModel.showCornealSubField.observe(viewLifecycleOwner) { show ->
            binding.llCornealSubfield.visibility = if (show) View.VISIBLE else View.GONE
            if (!show) binding.actvCornealDiseaseType.setText("")
        }
        viewModel.showVitaminASubField.observe(viewLifecycleOwner) { show ->
            binding.llVitaminASubfield.visibility = if (show) View.VISIBLE else View.GONE
            if (!show) binding.rgVitaminADeficiency.clearCheck()
        }
    }

    private fun observeSubFieldAlerts() {
        viewModel.showCataractAlert.observe(viewLifecycleOwner) { show ->
            binding.tvCataractAlert.visibility = if (show) View.VISIBLE else View.GONE
        }
        viewModel.showGlaucomaAlert.observe(viewLifecycleOwner) { show ->
            binding.tvGlaucomaAlert.visibility = if (show) View.VISIBLE else View.GONE
        }
        viewModel.showDrAlert.observe(viewLifecycleOwner) { show ->
            binding.tvDrAlert.visibility = if (show) View.VISIBLE else View.GONE
        }
        viewModel.showPresbyopiaAlert.observe(viewLifecycleOwner) { show ->
            binding.tvPresbyopiaAlert.visibility = if (show) View.VISIBLE else View.GONE
        }
        viewModel.showTrachomaAlert.observe(viewLifecycleOwner) { show ->
            binding.tvTrachomaAlert.visibility = if (show) View.VISIBLE else View.GONE
        }
        viewModel.showCornealAlert.observe(viewLifecycleOwner) { show ->
            binding.tvCornealAlert.visibility = if (show) View.VISIBLE else View.GONE
        }
        viewModel.showVitaminAAlert.observe(viewLifecycleOwner) { show ->
            binding.tvVitaminAAlert.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    private fun observeSubFieldRestoration() {
        viewModel.cataractSymptoms.observe(viewLifecycleOwner) { value ->
            applyRadioState(binding.rgCataractSymptoms, value, R.id.rb_cataract_yes, R.id.rb_cataract_no)
        }
        viewModel.glaucomaSymptoms.observe(viewLifecycleOwner) { value ->
            applyRadioState(binding.rgGlaucomaSymptoms, value, R.id.rb_glaucoma_yes, R.id.rb_glaucoma_no)
        }
        viewModel.drSymptoms.observe(viewLifecycleOwner) { value ->
            applyRadioState(binding.rgDrSymptoms, value, R.id.rb_dr_yes, R.id.rb_dr_no)
        }
        viewModel.presbyopiaSymptoms.observe(viewLifecycleOwner) { value ->
            applyRadioState(binding.rgPresbyopiaSymptoms, value, R.id.rb_presbyopia_yes, R.id.rb_presbyopia_no)
        }
        viewModel.vitaminADeficiency.observe(viewLifecycleOwner) { value ->
            applyRadioState(binding.rgVitaminADeficiency, value, R.id.rb_vitamin_a_yes, R.id.rb_vitamin_a_no)
        }
        viewModel.trachomaStatus.observe(viewLifecycleOwner) { value ->
            if (!value.isNullOrEmpty() && binding.actvTrachomaStatus.text.toString() != value) {
                binding.actvTrachomaStatus.setText(value, false)
            }
        }
        viewModel.cornealDiseaseType.observe(viewLifecycleOwner) { value ->
            if (!value.isNullOrEmpty() && binding.actvCornealDiseaseType.text.toString() != value) {
                binding.actvCornealDiseaseType.setText(value, false)
            }
        }
    }

    private fun applyRadioState(
        group: android.widget.RadioGroup,
        value: Boolean?,
        yesId: Int,
        noId: Int
    ) {
        when (value) {
            true -> group.check(yesId)
            false -> group.check(noId)
            null -> group.clearCheck()
        }
    }

    private fun applyIsDiabeticState(isDiabetic: Boolean?) {
        when (isDiabetic) {
            true  -> binding.rgIsDiabetic.check(R.id.rb_diabetic_yes)
            false -> binding.rgIsDiabetic.check(R.id.rb_diabetic_no)
            null  -> binding.rgIsDiabetic.clearCheck()
        }
        binding.llScreeningSection.visibility = if (isDiabetic == true) View.VISIBLE else View.GONE
    }

    private fun applyScreeningPerformedState(performed: Boolean?) {
        when (performed) {
            true  -> binding.rgScreeningPerformed.check(R.id.rb_screening_yes)
            false -> binding.rgScreeningPerformed.check(R.id.rb_screening_no)
            null  -> binding.rgScreeningPerformed.clearCheck()
        }
    }

    override fun getFragmentId(): Int = R.id.ophthalmicScreeningFragment

    override fun onSubmitAction() { navigateAfterSave() }

    override fun onCancelAction() { findNavController().popBackStack() }
}