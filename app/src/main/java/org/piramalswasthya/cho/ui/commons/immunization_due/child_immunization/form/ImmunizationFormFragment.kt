package org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.work.WorkerUtils
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.form.ImmunizationFormViewModel.State
import timber.log.Timber

@AndroidEntryPoint
class ImmunizationFormFragment : Fragment(), NavigationAdapter, FhirFragmentService {
    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!


    override val viewModel: ImmunizationFormViewModel by viewModels()

    override var fragmentContainerId: Int = 0

    override val fragment: Fragment = this

    override val jsonFile: String = "patient-visit-details-paginated.json"
    override fun navigateNext() {
        submitImmForm()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.btnNext.visibility = View.INVISIBLE
//        binding.btnPrev.visibility = View.INVISIBLE
//        binding.btnSubmitForm.visibility = View.VISIBLE
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            submitImmForm()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
                }

                State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, resources.getString(R.string.save_successful), Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                    findNavController().navigateUp()
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(
                        context, resources.getString(R.string.something_wend_wong_contact_testing), Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }

        binding.fabEdit.setOnClickListener {
            viewModel.updateRecordExists(false)
        }


        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    isEnabled = !recordExists
                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())
                            adapter.submitList(it)
                    }
                }
            }
        }
    }

    private fun submitImmForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun validateCurrentPage(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1) true
        else {
            if (result != null) {
                binding.form.rvInputForm.scrollToPosition(result)
            }
            false
        }
    }

    override fun onStart() {
        super.onStart()
//        activity?.let {
//            (it as HomeActivity).updateActionBar(R.drawable.ic__immunization, getString(R.string.immunization))
//        }
    }


    override fun getFragmentId(): Int {
        return R.id.fragment_immunization
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }


}