package org.piramalswasthya.cho.ui.elder_health

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
import org.piramalswasthya.cho.databinding.FragmentPsychosocialCaregiverSupportFormBinding
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.ui.commons.BaseAssessmentFormFragment
import org.piramalswasthya.cho.work.WorkerUtils

@AndroidEntryPoint
class PsychosocialCaregiverSupportFormFragment :
    BaseAssessmentFormFragment<PsychosocialCaregiverSupportFormViewModel>() {

    private var _binding: FragmentPsychosocialCaregiverSupportFormBinding? = null
    private val binding get() = _binding!!

    override val viewModel: PsychosocialCaregiverSupportFormViewModel by viewModels()

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
        getString(R.string.title_psychosocial_caregiver_support)

    override fun getSaveSuccessMessage(): String =
        getString(R.string.psychosocial_caregiver_support_saved)

    override fun getFormFlow(): Flow<List<FormElement>> =
        viewModel.formList

    override fun onUpdateFormValue(formId: Int, index: Int) =
        viewModel.updateListOnValueChanged(formId, index)

    override fun onSaveForm() =
        viewModel.saveForm()

    override fun getFragmentId(): Int =
        R.id.fragment_psychosocial_caregiver_support_form

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentPsychosocialCaregiverSupportFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()   // restores bottom navigation
        _binding = null
    }

    // Stamp Psychosocial Caregiver Support metadata onto MasterDb from arguments and navigate to the vitals screen.
    override fun onSaveSuccess() {
        WorkerUtils.psychosocialCaregiverSupport(requireContext())
        val masterDb = arguments?.getSerializable("MasterDb") as? org.piramalswasthya.cho.model.MasterDb
            ?: org.piramalswasthya.cho.model.MasterDb(patientId = arguments?.getString("patientID") ?: "", visitMasterDb = org.piramalswasthya.cho.model.VisitMasterDb())
        masterDb.visitMasterDb?.apply {
            category = "Other CPHC Services"
            subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.psychosocialCaregiverSupport
            reason = org.piramalswasthya.cho.ui.commons.DropdownConst.psychosocialCaregiverSupport
        }
        val bundle = android.os.Bundle().apply { putSerializable("MasterDb", masterDb) }
        findNavController().navigate(org.piramalswasthya.cho.R.id.customVitalsFragment, bundle)
    }
}