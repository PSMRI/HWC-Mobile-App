package org.piramalswasthya.cho.ui.ENT

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNoseDiagnosisFormBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.utils.KeyboardUtils

@AndroidEntryPoint
class NoseDiagnosisFormFragment : Fragment(), NavigationAdapter {

    private var _binding: FragmentNoseDiagnosisFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoseDiagnosisFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoseDiagnosisFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        setupForm()
        observeViewModel()

        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }

        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }

        binding.btnSubmit.setOnClickListener { submitForm() }
        binding.btnCancel.setOnClickListener { onCancelAction() }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onCancelAction()
        }
    }

    private fun setupForm() {

        val adapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                viewModel.updateListOnValueChanged(formId, index)
            },
            isEnabled = true
        )

        binding.form.rvInputForm.layoutManager =
            LinearLayoutManager(requireContext())

        binding.form.rvInputForm.adapter = adapter
        binding.form.rvInputForm.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    view.findViewById<AutoCompleteTextView>(R.id.actv_rv_dropdown)
                        ?.let { applyDropdownOpenOnClick(it) }
                }
                override fun onChildViewDetachedFromWindow(view: View) {}
            }
        )

        lifecycleScope.launch {
            viewModel.formList.collect { list ->
                if (list.isNotEmpty()) {
                    adapter.submitList(list)
                    binding.form.rvInputForm.post { reapplyDropdownListeners() }
                }
            }
        }

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.title_nose_diagnosis)

        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    /** Set on every ACTV found in currently visible RecyclerView children. */
    private fun reapplyDropdownListeners() {
        val rv = binding.form.rvInputForm
        for (i in 0 until rv.childCount) {
            rv.getChildAt(i)
                ?.findViewById<AutoCompleteTextView>(R.id.actv_rv_dropdown)
                ?.let { applyDropdownOpenOnClick(it) }
        }
    }

    
    @SuppressLint("ClickableViewAccessibility")
    private fun applyDropdownOpenOnClick(actv: AutoCompleteTextView) {
        actv.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    KeyboardUtils.hideKeyboard(actv)
                    KeyboardUtils.hideKeyboardFromActivity(actv.context)
                    false   
                }
                MotionEvent.ACTION_UP -> {
                    if (actv.isPopupShowing) actv.dismissDropDown()
                    else actv.showDropDown()
                    true    
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                NoseDiagnosisFormViewModel.State.IDLE -> Unit

                NoseDiagnosisFormViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                NoseDiagnosisFormViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Nose Diagnosis Saved", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }

                NoseDiagnosisFormViewModel.State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.showAlert.observe(viewLifecycleOwner) { message ->
            message?.let {
                AlertDialog.Builder(requireContext())
                    .setTitle("Alert")
                    .setMessage(it)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.clearAlert()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun submitForm() {
        val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter ?: return

        val result = adapter.validateInput(resources)
        if (result == -1) {
            viewModel.saveForm()
        } else {
            binding.form.rvInputForm.scrollToPosition(result)
        }
    }

    override fun onSubmitAction() {
        submitForm()
    }

    override fun onCancelAction() {
        if (!findNavController().navigateUp()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun getFragmentId(): Int =
        R.id.fragment_nose_diagnosis_form

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
