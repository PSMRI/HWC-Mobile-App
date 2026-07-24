package org.piramalswasthya.cho.ui.oral_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOralHealthFormBinding
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.ui.commons.BaseAssessmentFormFragment
import org.piramalswasthya.cho.ui.commons.DropdownConst
import org.piramalswasthya.cho.ui.commons.PendingCphcFormViewModel

@AndroidEntryPoint
class OralHealthFormFragment : BaseAssessmentFormFragment<OralHealthFormViewModel>() {

    private var _binding: FragmentOralHealthFormBinding? = null
    private val binding get() = _binding!!

    private val args: OralHealthFormFragmentArgs by navArgs()

    override val viewModel: OralHealthFormViewModel by viewModels()

    override val inputFormRecyclerView: RecyclerView get() = binding.form.rvInputForm
    override val contentLayout: View get() = binding.llContent
    override val progressBar: View get() = binding.pbForm
    override val benNameTextView: TextView get() = binding.tvBenName
    override val ageGenderTextView: TextView get() = binding.tvAgeGender
    override val submitButton: View get() = binding.btnSubmit
    override val cancelButton: View get() = binding.btnCancel

    override fun getFormTitle(): String = getString(R.string.title_oral_health)
    override fun getSaveSuccessMessage(): String = getString(R.string.oral_health_saved)
    override fun getFormFlow(): Flow<List<FormElement>> = viewModel.formList
    override fun onUpdateFormValue(formId: Int, index: Int) =
        viewModel.updateListOnValueChanged(formId, index)
    override fun onSaveForm() = viewModel.saveForm()
    override fun getFragmentId(): Int = R.id.fragment_oral_health_form

    override val persistOnNext: Boolean = false

    private val pendingCphcFormViewModel: PendingCphcFormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOralHealthFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<TextView>(R.id.header_text_register_patient)?.text = getFormTitle()
    }

    // Stamp Oral Health visit metadata onto MasterDb from arguments and navigate to vitals.
    override fun onStageAndProceed() {
        viewModel.stagePendingSave(pendingCphcFormViewModel)
        navigateToCphcVitalsAfterSave(
            subCategory = DropdownConst.oral,
            reasonForVisit = DropdownConst.dental,
            requireMasterDb = true,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
