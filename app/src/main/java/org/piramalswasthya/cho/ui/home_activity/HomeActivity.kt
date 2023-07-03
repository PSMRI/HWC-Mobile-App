package org.piramalswasthya.cho.ui.home_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.ActivityHomeBinding
import org.piramalswasthya.cho.list.benificiaryList
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.model.PatientListAdapter
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.visit_details.VisitDetailsFragment
//import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null

    private val binding: ActivityHomeBinding
        get() = _binding!!

    val patientDetails = PatientDetails()

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentUsernameBinding.inflate(layoutInflater, container, false)
//        return binding.root
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentVisitDetails = PersonalDetailsFragment();
        supportFragmentManager.beginTransaction().replace(binding.patientListFragment.id, fragmentVisitDetails).commit()
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

        // Set the adapter to the ListView
//        binding.listView.adapter = adapter

        binding.registration.setOnClickListener {
            val intent = Intent(this, RegisterPatientActivity::class.java)
            startActivity(intent)
        }

    }
}