package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ANC_VISITS_KEY
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.ui.home.rmncha.SubModuleActivity
import timber.log.Timber

@AndroidEntryPoint
class DeliveryOutcomeFormFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding ?: throw IllegalStateException("Fragment binding accessed after onDestroyView")

    private val viewModel: DeliveryOutcomeFormViewModel by viewModels()

    private val patientID: String
        get() = arguments?.getString("patientID") ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set patientID in ViewModel
        if (patientID.isNotEmpty()) {
            viewModel.setPatientID(patientID)
        } else {
            Timber.e("PatientID is empty, cannot initialize form")
            activity?.supportFragmentManager?.popBackStack()
            return
        }

        // Delivery Outcome–specific UI: form title and Case ID row
        binding.tvFormTitle.text = getString(R.string.delivery_outcome)
        binding.llCaseIdRow.visibility = View.VISIBLE

        // Set up all observers and UI components
        setupFormAdapter()
        setupPatientDetailsObservers()
        setupAncHistoryLink()
        setupAlertObserver()
        setupSubmitButton()
        setupStateObserver()
    }

    private fun setupFormAdapter() {
        var adapter: FormInputAdapter? = null
        var previousFormSize = 0

        // Observe form list and update adapter when it's ready
        lifecycleScope.launch {
            viewModel.formList.collect { formElements ->
                if (formElements.isNotEmpty() && adapter != null) {
                    val currentSize = formElements.size
                    adapter?.submitList(formElements)
                    
                    // Scroll to "Other (specify)" field (id = 9) when it's added
                    if (currentSize > previousFormSize) {
                        scrollToOtherFieldIfAdded(formElements)
                    }
                    previousFormSize = currentSize
                }
            }
        }

        // Observe record existence to enable/disable form and set up adapter
        viewModel.recordExists.observe(viewLifecycleOwner) { recordExists ->
            if (recordExists != null) {
                adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    isEnabled = !recordExists
                )
                binding.form.rvInputForm.adapter = adapter
                adapter?.submitList(viewModel.formList.value)
                binding.btnSubmit.isEnabled = !recordExists
                setupEditTextFocusListener()
            }
        }
    }

    private fun scrollToOtherFieldIfAdded(formElements: List<org.piramalswasthya.cho.model.FormElement>) {
        val otherFieldIndex = formElements.indexOfFirst { it.id == 9 }
        if (otherFieldIndex != -1) {
            binding.form.rvInputForm.post {
                binding.form.rvInputForm.scrollToPosition(otherFieldIndex)
                binding.form.rvInputForm.postDelayed({
                    val viewHolder = binding.form.rvInputForm.findViewHolderForAdapterPosition(otherFieldIndex)
                    viewHolder?.itemView?.findViewById<android.widget.EditText>(R.id.et)?.requestFocus()
                }, 300)
            }
        }
    }

    private fun setupEditTextFocusListener() {
        binding.form.rvInputForm.addOnChildAttachStateChangeListener(
            object : androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    view.findViewById<android.widget.EditText>(R.id.et)?.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            val position = binding.form.rvInputForm.getChildAdapterPosition(view)
                            if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                                binding.form.rvInputForm.post {
                                    binding.form.rvInputForm.smoothScrollToPosition(position)
                                }
                            }
                        }
                    }
                }
                override fun onChildViewDetachedFromWindow(view: View) {
                    // No action needed when view is detached
                }
            }
        )
    }

    private fun setupPatientDetailsObservers() {
        viewModel.benName.observe(viewLifecycleOwner) {
            it?.let { name -> binding.tvBenName.text = name }
        }

        viewModel.benAge.observe(viewLifecycleOwner) {
            it?.let { age -> binding.tvAgeGender.text = age }
        }

        viewModel.caseId.observe(viewLifecycleOwner) {
            it?.let { caseId -> binding.tvCaseId.text = caseId }
        }
    }

    private fun setupAncHistoryLink() {
        binding.tvViewAncHistory.visibility = View.VISIBLE
        binding.tvViewAncHistory.setOnClickListener {
            val intent = SubModuleActivity.getDirectFragmentIntent(requireContext(), SHOW_ANC_VISITS_KEY)
            startActivity(intent)
        }
    }

    private fun setupAlertObserver() {
        lifecycleScope.launch {
            viewModel.alertMessageFlow.collect { alertMessage ->
                alertMessage?.let { message ->
                    if (isAdded) {
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_popup))
                            .setMessage(message)
                            .setPositiveButton(getString(android.R.string.ok)) { dialog, _ -> dialog.dismiss() }
                            .setCancelable(true)
                            .create()
                        dialog.setOnDismissListener { viewModel.clearAlertMessage() }
                        dialog.show()
                    }
                }
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            submitDeliveryOutcomeForm()
        }
    }

    private fun setupStateObserver() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                DeliveryOutcomeFormViewModel.State.IDLE -> {
                    // Do nothing
                }
                DeliveryOutcomeFormViewModel.State.LOADING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }
                DeliveryOutcomeFormViewModel.State.LOAD_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    context?.let {
                        Toast.makeText(it, "Error loading form. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
                DeliveryOutcomeFormViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }
                DeliveryOutcomeFormViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    context?.let {
                        Toast.makeText(it, "Save Successful", Toast.LENGTH_LONG).show()
                    }
                    parentFragmentManager.popBackStack()
                }
                DeliveryOutcomeFormViewModel.State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    context?.let {
                        Toast.makeText(it, "Something went wrong! Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun submitDeliveryOutcomeForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun validateCurrentPage(): Boolean {
        val result: Int? = binding.form.rvInputForm.adapter?.let {
            (it as? FormInputAdapter)?.validateInput(resources, binding.form.rvInputForm)
        }
        Timber.d("Validation result: $result")
        return if (result == -1) {
            true
        } else {
            result?.let {
                binding.form.rvInputForm.scrollToPosition(it)
            }
            false
        }
    }

    override fun onStart() {
        super.onStart()
        // Update toolbar title - parent Activity will handle this
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = 
            getString(R.string.delivery_outcome)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
