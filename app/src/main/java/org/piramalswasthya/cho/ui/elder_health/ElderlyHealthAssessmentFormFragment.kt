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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import org.piramalswasthya.cho.work.WorkerUtils


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
    override fun getSaveSuccessMessage(): String = getString(R.string.elderly_health_assessment_saved)
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

    // Data is already saved by the time we get here. When a specific screening recommended a
    // referral but the CHO chose "No", show the recheck alert so the CHO can decide whether to
    // revisit the referral answer or continue. Otherwise proceed straight to the vitals screen.
    override fun onSaveSuccess() {
        val recommended = viewModel.specificScreeningsRecommendingReferralIfNoSelected()
        if (recommended.isNotEmpty()) {
            showReferralRecheckAlert(recommended)
            return
        }
        proceedAfterSave()
    }

    private fun proceedAfterSave() {
        WorkerUtils.elderlyPushWorker(requireContext())
        navigateToCphcVitalsAfterSave(
            subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.elderlyHealthAssessment,
        )
    }

    // Yes → dismiss and stay on the page so the CHO can change the referral answer.
    // No  → keep referral = No and move on to the next screen.
    private fun showReferralRecheckAlert(screenings: List<String>) {
        if (!isAdded) return
        val message = buildString {
            append(getString(R.string.elderly_referral_recheck_message))
            screenings.forEachIndexed { index, name ->
                append("\n")
                append(index + 1)
                append(". ")
                append(name)
            }
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.form_alert_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ -> dialog.dismiss() }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
                proceedAfterSave()
            }
            .setCancelable(false)
            .show()
    }
}
