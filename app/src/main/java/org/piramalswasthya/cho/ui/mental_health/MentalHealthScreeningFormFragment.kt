package org.piramalswasthya.cho.ui.mental_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentMentalHealthScreeningFormBinding
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.ui.commons.BaseAssessmentFormFragment

@AndroidEntryPoint
class MentalHealthScreeningFormFragment :
    BaseAssessmentFormFragment<MentalHealthScreeningFormViewModel>() {

    private var _binding: FragmentMentalHealthScreeningFormBinding? = null
    private val binding get() = _binding!!

    override val viewModel: MentalHealthScreeningFormViewModel by viewModels()

    // ── View references ───────────────────────────────────────────────────────

    override val inputFormRecyclerView: RecyclerView
        get() = binding.form.rvInputForm

    override val contentLayout: View
        get() = binding.llContent

    override val progressBar: View
        get() = binding.pbForm

    override val benNameTextView: TextView
        get() = binding.tvBenName

    override val ageGenderTextView: TextView
        get() = binding.tvAgeGender

    override val submitButton: View
        get() = binding.btnSubmit

    override val cancelButton: View
        get() = binding.btnCancel

    // ── Form-specific configuration ──────────────────────────────────────────

    override fun getFormTitle(): String =
        getString(R.string.title_mental_health_screening)

    override fun getSaveSuccessMessage(): String =
        getString(R.string.mental_health_screening_saved)

    override fun getFormFlow(): Flow<List<FormElement>> =
        viewModel.formList

    override fun onUpdateFormValue(formId: Int, index: Int) =
        viewModel.updateListOnValueChanged(formId, index)

    override fun onSaveForm() =
        viewModel.saveForm()

    override fun getFragmentId(): Int =
        R.id.fragment_mental_health_screening_form

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentMentalHealthScreeningFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePhq9Alert()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Stamp Mental Health Screening metadata onto MasterDb from arguments and navigate to the vitals screen.
    override fun onSaveSuccess() {
        val masterDb = arguments?.getSerializable("MasterDb") as? org.piramalswasthya.cho.model.MasterDb
            ?: org.piramalswasthya.cho.model.MasterDb(patientId = arguments?.getString("patientID") ?: "", visitMasterDb = org.piramalswasthya.cho.model.VisitMasterDb())
        masterDb.visitMasterDb?.apply {
            category = "Other CPHC Services"
            subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.mentalHealthScreening
            reason = org.piramalswasthya.cho.ui.commons.DropdownConst.mentalHealthScreening
        }
        val bundle = android.os.Bundle().apply { putSerializable("MasterDb", masterDb) }
        findNavController().navigate(org.piramalswasthya.cho.R.id.customVitalsFragment, bundle)
    }

    private fun observePhq9Alert() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.phq9AlertMessageFlow.collect { message ->
                    message?.let {
                        if (isAdded) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.form_alert_title))
                                .setMessage(it)
                                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                    dialog.dismiss()
                                    viewModel.clearPhq9AlertMessage()
                                }
                                .setCancelable(false)
                                .show()
                            }

                    }
                }
            }
        }
    }
}