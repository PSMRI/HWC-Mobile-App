package org.piramalswasthya.cho.ui.home_activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityDashboardBinding
import org.piramalswasthya.cho.databinding.FragmentDashboardBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.NavigationAdapter


@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    val patientDetails = PatientDetails()

    private var _binding: ActivityDashboardBinding? = null

    private val binding: ActivityDashboardBinding
        get() = _binding!!

    private lateinit var currFragment: NavigationAdapter

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDashboardBinding.inflate(layoutInflater)
        title = "Dashboard";
        setContentView(binding.root)

        val months = resources.getStringArray(R.array.months)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(this, R.layout.drop_down, months)
        // get reference to the autocomplete text view
        val autocompleteTV = findViewById<AutoCompleteTextView>(R.id.month_report)
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV.setAdapter(arrayAdapter)

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

    override fun onBackPressed() {
        finish()
    }

}