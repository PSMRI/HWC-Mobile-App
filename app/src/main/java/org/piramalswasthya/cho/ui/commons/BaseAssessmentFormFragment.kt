package org.piramalswasthya.cho.ui.commons

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.model.FormElement

/**
 * Base [Fragment] for single-page assessment / diagnosis forms.
 *
 * Concrete subclasses must:
 *  - provide view references via the abstract `val` properties (delegating to their
 *    specific ViewBinding after inflation)
 *  - implement the handful of abstract methods that carry form-specific values
 *  - call `super.onDestroyView()` **before** nulling their `_binding`
 *
 * @param VM  A [BaseFormViewModel] subclass that drives this form.
 */
abstract class BaseAssessmentFormFragment<VM : BaseFormViewModel> : Fragment(), NavigationAdapter {

    // ── Required ViewModel ────────────────────────────────────────────────────

    protected abstract val viewModel: VM

    // ── View references (delegated to concrete binding in subclass) ───────────

    protected abstract val inputFormRecyclerView: RecyclerView
    protected abstract val contentLayout: View
    protected abstract val progressBar: View
    protected abstract val benNameTextView: TextView
    protected abstract val ageGenderTextView: TextView
    protected abstract val submitButton: View
    protected abstract val cancelButton: View

    // ── Form-specific overrides ───────────────────────────────────────────────

    /** ActionBar title for this form. */
    protected abstract fun getFormTitle(): String

    /** Toast text shown on SAVE_SUCCESS. */
    protected abstract fun getSaveSuccessMessage(): String

    /** StateFlow / SharedFlow of [FormElement] lists supplied by the ViewModel. */
    protected abstract fun getFormFlow(): Flow<List<FormElement>>

    /** Called when a form value changes; delegates to ViewModel. */
    protected abstract fun onUpdateFormValue(formId: Int, index: Int)

    /** Triggers the ViewModel save. */
    protected abstract fun onSaveForm()

    // ── Back-press ────────────────────────────────────────────────────────────

    protected val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = onCancelAction()
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, onBackPressedCallback)

        setupFormAdapter()
        observeState()
        observeAlert()

        viewModel.benName.observe(viewLifecycleOwner) { benNameTextView.text = it }
        viewModel.benAgeGender.observe(viewLifecycleOwner) { ageGenderTextView.text = it }

        submitButton.setOnClickListener { submitForm() }
        cancelButton.setOnClickListener { onCancelAction() }
    }

    // ── Form adapter ──────────────────────────────────────────────────────────

    private fun setupFormAdapter() {
        val adapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                onUpdateFormValue(formId, index)
            },
            isEnabled = true
        )

        inputFormRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        inputFormRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            getFormFlow().collect { list ->
                if (list.isNotEmpty()) {
                    adapter.submitList(list.toMutableList()) {
                        //This are required to reflected without requiring manual scroll.
                        inputFormRecyclerView.post { adapter.notifyDataSetChanged() }
                    }
                }
            }
        }

        (activity as? AppCompatActivity)?.supportActionBar?.title = getFormTitle()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    // ── State / alert observers ───────────────────────────────────────────────

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                BaseFormViewModel.State.IDLE -> Unit

                BaseFormViewModel.State.SAVING -> {
                    contentLayout.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                }

                BaseFormViewModel.State.SAVE_SUCCESS -> {
                    contentLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), getSaveSuccessMessage(), Toast.LENGTH_LONG).show()
                    viewModel.resetState() // Reset to IDLE so back-stack re-delivery does not retrigger onSaveSuccess().
                    onSaveSuccess()
                }

                BaseFormViewModel.State.SAVE_FAILED -> {
                    contentLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), getString(R.string.form_save_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /** Override in subclasses to customise post-save navigation. Default: pop back. */
    protected open fun onSaveSuccess() {
        findNavController().navigateUp()
    }

    private fun observeAlert() {
        viewModel.showAlert.observe(viewLifecycleOwner) { message ->
            message?.let { showAlertDialog(it) }
        }
    }

    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.alert_popup))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button)) { dialog, _ ->
                dialog.dismiss()
                viewModel.clearAlert()
            }
            .setCancelable(false)
            .show()
    }


    protected fun submitForm() {
        val adapter = inputFormRecyclerView.adapter as? FormInputAdapter ?: return
        val result = adapter.validateInput(resources)
        if (result == -1) {
            onSaveForm()
        } else {
            val fieldName = adapter.currentList.getOrNull(result)?.title ?: getString(R.string.form_input_empty_error)
            Toast.makeText(
                requireContext(),
                getString(R.string.form_fill_field_error, fieldName),
                Toast.LENGTH_SHORT
            ).show()
            inputFormRecyclerView.scrollToPosition(result)
        }
    }

    override fun onSubmitAction() = submitForm()

    override fun onCancelAction() {
        onBackPressedCallback.isEnabled = false
        if (!findNavController().navigateUp()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }
}
