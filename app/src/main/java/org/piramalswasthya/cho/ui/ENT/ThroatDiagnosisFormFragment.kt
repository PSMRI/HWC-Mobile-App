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
import org.piramalswasthya.cho.databinding.FragmentThroatDiagnosisFormBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class ThroatDiagnosisFormFragment : Fragment(), NavigationAdapter {

    private var _binding: FragmentThroatDiagnosisFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThroatDiagnosisFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThroatDiagnosisFormBinding.inflate(inflater, container, false)
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
            getString(R.string.title_throat_diagnosis)

        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    private fun observeViewModel() {

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ThroatDiagnosisFormViewModel.State.IDLE -> Unit

                ThroatDiagnosisFormViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                ThroatDiagnosisFormViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Throat Diagnosis Saved", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }

                ThroatDiagnosisFormViewModel.State.SAVE_FAILED -> {
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

        viewModel.triggerMultiSelect.observe(viewLifecycleOwner) { data ->
            data?.let {
                val selectedItemsList = mutableListOf<String>()
                AlertDialog.Builder(requireContext())
                    .setTitle(it.title)
                    .setMultiChoiceItems(it.items, it.selectedItems) { _, which, isChecked ->
                        it.selectedItems[which] = isChecked
                    }
                    .setPositiveButton("OK") { _, _ ->
                        for (i in it.selectedItems.indices) {
                            if (it.selectedItems[i]) {
                                selectedItemsList.add(it.items[i])
                            }
                        }
                        viewModel.updateMultiSelectValue(data.formId, selectedItemsList)
                        val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter
                        adapter?.let { adp ->
                            val position = adp.currentList.indexOfFirst { it.id == data.formId }
                            if (position != -1) {
                                adp.notifyItemChanged(position)
                            }
                        }
                        viewModel.onMultiSelectDialogDismissed()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.onMultiSelectDialogDismissed()
                    }
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
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun getFragmentId(): Int =
        R.id.throatDiagnosisFormFragment

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}