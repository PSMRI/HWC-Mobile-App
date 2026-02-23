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
import org.piramalswasthya.cho.databinding.FragmentEarDiagnosisFormBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class EarDiagnosisFormFragment : Fragment(), NavigationAdapter {

    private var _binding: FragmentEarDiagnosisFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EarDiagnosisFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEarDiagnosisFormBinding.inflate(inflater, container, false)
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
            getString(R.string.title_ear_diagnosis)

        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    private fun observeViewModel() {

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                EarDiagnosisFormViewModel.State.IDLE -> Unit

                EarDiagnosisFormViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                EarDiagnosisFormViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Ear Diagnosis Saved", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }

                EarDiagnosisFormViewModel.State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save failed", Toast.LENGTH_LONG).show()
                }
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
        onBackPressedCallback.isEnabled = false
        if (!findNavController().navigateUp()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun getFragmentId(): Int =
        R.id.fragment_ear_diagnosis_form

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        _binding = null
    }
}