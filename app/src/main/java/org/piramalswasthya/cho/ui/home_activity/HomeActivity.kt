package org.piramalswasthya.cho.ui.home_activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityHomeBinding
import org.piramalswasthya.cho.list.benificiaryList
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.model.PatientListAdapter
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FhirVisitDetailsFragment
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.home.HomeFragment
//import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null

    private val binding: ActivityHomeBinding
        get() = _binding!!
    private lateinit var navController: NavController

    private val viewModel: HomeActivityViewModel by viewModels()

    val patientDetails = PatientDetails()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.homeFragment) as NavHostFragment
        navController = navHostFragment.navController
        viewModel.updateLastSyncTimestamp()

        // Create an ArrayList to hold your data
        val dataList = ArrayList<String>()
        dataList.add("Item 1")
        dataList.add("Item 2")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")

        // Create an ArrayAdapter to bind the data to the ListView
        var adapter = PatientListAdapter(this, benificiaryList)

    }
}