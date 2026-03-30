package org.piramalswasthya.cho.ui.ENT

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentThroatDiagnosisFormBinding
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.ui.commons.BaseAssessmentFormFragment

@AndroidEntryPoint
class ThroatDiagnosisFormFragment :
    BaseAssessmentFormFragment<ThroatDiagnosisFormViewModel>() {

    private var _binding: FragmentThroatDiagnosisFormBinding? = null
    private val binding get() = _binding!!

    override val viewModel: ThroatDiagnosisFormViewModel by viewModels()

    // ── View references ───────────────────────────────────────────────────────

    override val inputFormRecyclerView: RecyclerView get() = binding.form.rvInputForm
    override val contentLayout: View get() = binding.llContent
    override val progressBar: View get() = binding.pbForm
    override val benNameTextView: TextView get() = binding.tvBenName
    override val ageGenderTextView: TextView get() = binding.tvAgeGender
    override val submitButton: View get() = binding.btnSubmit
    override val cancelButton: View get() = binding.btnCancel

    // ── Form-specific values ──────────────────────────────────────────────────

    override fun getFormTitle(): String = getString(R.string.title_throat_diagnosis)
    override fun getSaveSuccessMessage(): String = getString(R.string.throat_diagnosis_saved)
    override fun getFormFlow(): Flow<List<FormElement>> = viewModel.formList
    override fun onUpdateFormValue(formId: Int, index: Int) =
        viewModel.updateListOnValueChanged(formId, index)
    override fun onSaveForm() = viewModel.saveForm()
    override fun getFragmentId(): Int = R.id.throatDiagnosisFormFragment

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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
        observeMultiSelect()
    }

    // ── Throat-specific: multi-select dialog ──────────────────────────────────

    private fun observeMultiSelect() {
        viewModel.triggerMultiSelect.observe(viewLifecycleOwner) { data ->
            data ?: return@observe
            val selectedItemsList = mutableListOf<String>()
            AlertDialog.Builder(requireContext())
                .setTitle(data.title)
                .setMultiChoiceItems(data.items, data.selectedItems) { _, which, isChecked ->
                    data.selectedItems[which] = isChecked
                }
                .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    for (i in data.selectedItems.indices) {
                        if (data.selectedItems[i]) selectedItemsList.add(data.items[i])
                    }
                    viewModel.updateMultiSelectValue(data.formId, selectedItemsList)
                    val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter
                    adapter?.let { adp ->
                        val position = adp.currentList.indexOfFirst { it.id == data.formId }
                        if (position != -1) adp.notifyItemChanged(position)
                    }
                    viewModel.onMultiSelectDialogDismissed()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                    viewModel.onMultiSelectDialogDismissed()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveSuccess() {
        val masterDb = arguments?.getSerializable("MasterDb") as? org.piramalswasthya.cho.model.MasterDb
            ?: org.piramalswasthya.cho.model.MasterDb(patientId = arguments?.getString("patientID") ?: "", visitMasterDb = org.piramalswasthya.cho.model.VisitMasterDb())
        masterDb.visitMasterDb?.apply {
            category = "Other CPHC Services"
            subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.throat
            reason = org.piramalswasthya.cho.ui.commons.DropdownConst.throat
        }
        val bundle = android.os.Bundle().apply { putSerializable("MasterDb", masterDb) }
        findNavController().navigate(org.piramalswasthya.cho.R.id.customVitalsFragment, bundle)
    }
}
