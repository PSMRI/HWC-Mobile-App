package org.piramalswasthya.cho.ui.commons.history_custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentHistoryCustomBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class HistoryCustomFragment : Fragment(R.layout.fragment_history_custom), NavigationAdapter {

    private val ILLNESS_OPTIONS = arrayOf(
                "Chicken Pox",
                "Dengue Fever",
                "Dysentry",
                "Filariasis",
                "Hepatitis(jaundice)",
                "Hepatitis B",
                "Malaria",
                "Measles",
                "Nill",
                "Other",
                "Pneumonis",
                "STI/RTI",
                "Tuberculosis",
                "Thyroid Fever"
    )

    private val TimePeriodAgo = arrayOf(
                "Day(s)",
                "Week(s)",
                "Month(s)",
                "Year(s)"
    )
    private var _binding: FragmentHistoryCustomBinding? = null
    private val binding: FragmentHistoryCustomBinding
        get() = _binding!!

    private lateinit var dropdownIllness: AutoCompleteTextView
    private lateinit var dropdownTimePeriodAgo: AutoCompleteTextView

    private val viewModel:HistoryCustomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryCustomBinding.inflate(inflater, container, false)
        val view = binding.root

        dropdownIllness = binding.illnessText
        dropdownTimePeriodAgo = binding.timePeriodAgoText
        // Create ArrayAdapter with the illness options and set it to the AutoCompleteTextView for "Illness"
        val illnessAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ILLNESS_OPTIONS)
        dropdownIllness.setAdapter(illnessAdapter)
        val timePeriodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,TimePeriodAgo)
        dropdownTimePeriodAgo.setAdapter(timePeriodAdapter)

        return view
    }

    override fun getFragmentId(): Int {
      return R.id.fragment_history_custom
    }

    override fun onSubmitAction() {
      navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigate(
            HistoryCustomFragmentDirections.actionHistoryCustomFragmentToFhirVisitDetailsFragment()
        )
    }
    fun navigateNext(){
        findNavController().navigate(
            HistoryCustomFragmentDirections.actionHistoryCustomFragmentToFhirVitalsFragment()
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}