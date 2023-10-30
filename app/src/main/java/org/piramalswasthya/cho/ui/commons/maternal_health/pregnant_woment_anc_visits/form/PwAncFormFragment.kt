package org.piramalswasthya.cho.ui.commons.maternal_health.pregnant_woment_anc_visits.form


import android.content.Intent
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
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.commons.maternal_health.pregnant_woment_anc_visits.form.PwAncFormViewModel.State
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class PwAncFormFragment() : Fragment(), NavigationAdapter, FhirFragmentService{

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    override val fragment = this

    override val viewModel: PwAncFormViewModel by viewModels()

    override var fragmentContainerId = 0

    override val jsonFile = "patient-visit-details-paginated.json"

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
                binding.fabEdit.visibility = /*if (recordExists) View.VISIBLE else */View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists
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
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            submitAncForm()
        }
        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
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
                    Toast.makeText(context, "Save Successful", Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                    findNavController().navigateUp()
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }
    }

    private fun submitAncForm() {
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
                    notifyItemChanged(viewModel.getIndexOfWeeksOfPregnancy())
                    notifyItemChanged(viewModel.getIndexOfWeeksOfPregnancy()+1)
                }
                16 ->{
                    notifyItemChanged(viewModel.getIndexOfTTBooster())
                }
                18 ->{
                    notifyItemChanged(viewModel.getIndexOfTT1())
                    notifyItemChanged(viewModel.getIndexOfTT2())

                }

//                9 ->{
//                    notifyItemChanged(viewModel.getIndexOfDiastolic())
//                    if(viewModel.triggerBpToggle() ){
//                        notifyItemChanged(viewModel.getIndexOfSystolic())
//                        viewModel.resetBpToggle()
//
//                    }
//                }
//                10 ->{
//                    notifyItemChanged(viewModel.getIndexOfSystolic())
//                    if(viewModel.triggerBpToggle()){
//                        notifyItemChanged(viewModel.getIndexOfDiastolic())
//                        viewModel.resetBpToggle()
//
//                    }
//                }
//                19 -> notifyItemChanged(viewModel.getIndexOfPastIllness())
            }
        }
    }
    override fun onStart() {
        super.onStart()
//        activity?.let {
//            (it as HomeActivity).updateActionBar(R.drawable.ic__pregnancy, getString(R.string.anc_visit))
//        }
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_new_form
    }


    override fun navigateNext() {
        submitAncForm()
//        findNavController().navigate(
//            R.id.action_fhirVisitDetailsFragment_to_pwAncFormFragment, bundle
//        )
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}