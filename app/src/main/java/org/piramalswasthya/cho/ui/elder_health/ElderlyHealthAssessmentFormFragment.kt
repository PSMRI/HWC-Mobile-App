package org.piramalswasthya.cho.ui.elder_health
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentElderlyHealthAssessmentFormBinding
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.ui.commons.BaseAssessmentFormFragment


@AndroidEntryPoint
class ElderlyHealthAssessmentFormFragment : BaseAssessmentFormFragment<ElderlyHealthAssessmentFormViewModel>() {

    private var _binding: FragmentElderlyHealthAssessmentFormBinding? = null
    private val binding get() = _binding!!

    override val viewModel: ElderlyHealthAssessmentFormViewModel by viewModels()

    override val inputFormRecyclerView: RecyclerView get() = binding.form.rvInputForm
    override val contentLayout: View get() = binding.llContent
    override val progressBar: View get() = binding.pbForm
    override val benNameTextView: TextView get() = binding.tvBenName
    override val ageGenderTextView: TextView get() = binding.tvAgeGender
    override val submitButton: View get() = binding.btnSubmit
    override val cancelButton: View get() = binding.btnCancel

    override fun getFormTitle(): String = getString(R.string.title_elderly_health_assessment)
    override fun getSaveSuccessMessage(): String = "Assessment Saved"
    override fun getFormFlow(): Flow<List<FormElement>> = viewModel.formList
    override fun onUpdateFormValue(formId: Int, index: Int) =
        viewModel.updateListOnValueChanged(formId, index)
    override fun onSaveForm() = viewModel.saveForm()
    override fun getFragmentId(): Int = R.id.fragment_elderly_health_assessment_form

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElderlyHealthAssessmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Preserve our explicit null-user error crash fix fallback 
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Stamp Elderly Health Assessment metadata onto MasterDb from arguments and navigate to the vitals screen.
    override fun onSaveSuccess() {
        val masterDb = arguments?.getSerializable("MasterDb") as? org.piramalswasthya.cho.model.MasterDb
            ?: org.piramalswasthya.cho.model.MasterDb(patientId = arguments?.getString("patientID") ?: "", visitMasterDb = org.piramalswasthya.cho.model.VisitMasterDb())
        masterDb.visitMasterDb?.apply {
            category = "Other CPHC Services"
            subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.elderlyHealthAssessment
            reason = org.piramalswasthya.cho.ui.commons.DropdownConst.elderlyHealthAssessment
        }
        val bundle = android.os.Bundle().apply { putSerializable("MasterDb", masterDb) }
        findNavController().navigate(org.piramalswasthya.cho.R.id.customVitalsFragment, bundle)
    }
}