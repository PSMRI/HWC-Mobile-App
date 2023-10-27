package org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form

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
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class EligibleCoupleTrackingFormFragment : Fragment(), NavigationAdapter, FhirFragmentService {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    override val viewModel: EligibleCoupleTrackingFormViewModel by viewModels()

    override var fragmentContainerId: Int = 0

    override val fragment: Fragment = this

    override val jsonFile: String = "patient-visit-details-paginated.json"
    override fun navigateNext() {
        submitEligibleTrackingForm()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
//                binding.fabEdit.visibility = if(recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists
                )
                binding.btnSubmit.isEnabled = !recordExists
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())
                            adapter.submitList(it)

                    }
                }
            }
        }
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            submitEligibleTrackingForm()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS -> {
                    navigateToNextScreen()
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                }

                else -> {}
            }
        }
    }

    private fun navigateToNextScreen() {
        if (viewModel.isPregnant) {
//            findNavController().navigate(
//                EligibleCoupleTrackingFormFragmentDirections.actionEligibleCoupleTrackingFormFragmentToPregnancyRegistrationFormFragment(
//                    benId = viewModel.benId
//                )
//            )
            viewModel.resetState()
        } else {
            findNavController().navigateUp()
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.tracking_form_filled_successfully),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetState()
        }
    }

    private fun submitEligibleTrackingForm() {
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


    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> {
                    notifyItemChanged(1)
                    notifyItemChanged(2)

                }
                4,5 -> {
                    notifyDataSetChanged()
                    //notifyItemChanged(viewModel.getIndexOfIsPregnant())
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
//        activity?.let {
//            (it as HomeActivity).updateActionBar(
//                R.drawable.ic__eligible_couple,
//                getString(R.string.eligible_couple_tracking_form)
//            )
//        }
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_new_form
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        submitEligibleTrackingForm()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}