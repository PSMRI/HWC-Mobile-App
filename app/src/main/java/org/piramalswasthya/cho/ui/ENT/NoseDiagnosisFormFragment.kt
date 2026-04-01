package org.piramalswasthya.cho.ui.ENT

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNoseDiagnosisFormBinding
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class NoseDiagnosisFormFragment : Fragment(), NavigationAdapter {

    private var _binding: FragmentNoseDiagnosisFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoseDiagnosisFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoseDiagnosisFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        setupForm()
        observeViewModel()

        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }

        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }

        binding.btnSubmit.setOnClickListener { submitForm() }
        binding.btnCancel.setOnClickListener { onCancelAction() }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onCancelAction()
        }
    }

    private fun setupForm() {

        val adapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                viewModel.updateListOnValueChanged(formId, index)
            },
            isEnabled = true
        )

        binding.form.rvInputForm.layoutManager =
            LinearLayoutManager(requireContext())

        binding.form.rvInputForm.adapter = adapter

        lifecycleScope.launch {
            viewModel.formList.collect { list ->
                if (list.isNotEmpty()) {
                    adapter.submitList(list)
                }
            }
        }

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.title_nose_diagnosis)

        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    private fun observeViewModel() {

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                BaseFormViewModel.State.IDLE -> Unit

                BaseFormViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                BaseFormViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Nose Diagnosis Saved", Toast.LENGTH_LONG).show()
                    // Stamp Nose visit metadata onto MasterDb and navigate to vitals; replaces the previous navigateUp() that bypassed visit registration.
                    val masterDb = arguments?.getSerializable("MasterDb") as? org.piramalswasthya.cho.model.MasterDb
                        ?: org.piramalswasthya.cho.model.MasterDb(patientId = arguments?.getString("patientID") ?: "", visitMasterDb = org.piramalswasthya.cho.model.VisitMasterDb())
                    masterDb.visitMasterDb?.apply {
                        category = "Other CPHC Services"
                        subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.nose
                        reason = org.piramalswasthya.cho.ui.commons.DropdownConst.nose
                    }
                    val bundle = android.os.Bundle().apply { putSerializable("MasterDb", masterDb) }
                    findNavController().navigate(org.piramalswasthya.cho.R.id.customVitalsFragment, bundle)
                }

                BaseFormViewModel.State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save failed", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        viewModel.showAlert.observe(viewLifecycleOwner) { message ->
            message?.let {
                AlertDialog.Builder(requireContext())
                    .setTitle("Alert")
                    .setMessage(it)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.clearAlert()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun submitForm() {
        val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter ?: return

        val result = adapter.validateInput(resources)
        if (result == -1) {
            viewModel.saveForm()
        } else {
            binding.form.rvInputForm.scrollToPosition(result)
        }
    }

    override fun onSubmitAction() {
        submitForm()
    }

    override fun onCancelAction() {
        if (!findNavController().navigateUp()) {
            onBackPressedCallback.remove()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun getFragmentId(): Int =
        R.id.fragment_nose_diagnosis_form

    override fun onDestroyView() {
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        super.onDestroyView()
        _binding = null
    }
}
