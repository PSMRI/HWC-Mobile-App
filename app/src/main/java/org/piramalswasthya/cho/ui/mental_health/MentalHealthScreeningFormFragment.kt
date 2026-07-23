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
import org.piramalswasthya.cho.work.WorkerUtils

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

    // Tracks the single informational alert shown when referral is detected via general screening.
    private var generalReferralAlertShown = false

    // Show one informational alert before submission when a referral is prompted purely by the
    // general screening questions. OK proceeds to save + navigate.
    override fun shouldProceedWithSubmit(): Boolean {
        if (!generalReferralAlertShown && viewModel.isGeneralReferralOnly() && isAdded) {
            generalReferralAlertShown = true
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.form_alert_title))
                .setMessage(getString(R.string.mh_referral_general_alert))
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    submitForm() // Re-run; flag is now set so submission proceeds.
                }
                .setCancelable(false)
                .show()
            return false
        }
        return true
    }

    // Data is already saved by the time we get here. When a specific screening recommended a
    // referral but the CHO chose "No", show the recheck alert and STAY on the page (block
    // navigation). Otherwise stamp metadata and navigate to the vitals screen.
    override fun onSaveSuccess() {
        val recommended = viewModel.specificScreeningsRecommendingReferralIfNoSelected()
        if (recommended.isNotEmpty()) {
            showReferralRecheckAlert(recommended)
            return
        }
        WorkerUtils.mentalPushWorker(requireContext())
        navigateToCphcVitalsAfterSave(
            subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.mentalHealth,
        )
    }

    private fun showReferralRecheckAlert(screenings: List<String>) {
        if (!isAdded) return
        val message = buildString {
            append(getString(R.string.mh_referral_recheck_message))
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
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
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
