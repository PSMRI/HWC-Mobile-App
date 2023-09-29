package org.piramalswasthya.cho.ui.home_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityDashboardBinding
import org.piramalswasthya.cho.databinding.FragmentDashboardBinding
import org.piramalswasthya.cho.databinding.FragmentHomeBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.NavigationAdapter


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val binding: FragmentDashboardBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val months = resources.getStringArray(R.array.months)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.drop_down, months)
        // get reference to the autocomplete text view
        // set adapter to the autocomplete tv to the arrayAdapter
        _binding!!.monthReport.setAdapter(arrayAdapter)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



//        binding.button.setOnClickListener {
//            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
//            currFragment.onSubmitAction()
//        }
//
//        binding.todayButton.setOnClickListener {
//            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
//            currFragment.onCancelAction()
//        }
    }

}