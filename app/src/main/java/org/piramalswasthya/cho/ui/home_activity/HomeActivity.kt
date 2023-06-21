package org.piramalswasthya.cho.ui.home_activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityAddPatientBinding
import org.piramalswasthya.cho.databinding.ActivityHomeBinding
import org.piramalswasthya.cho.databinding.FragmentUsernameBinding
import org.piramalswasthya.cho.model.PatientListAdapter
import org.piramalswasthya.cho.ui.add_patient_activity.AddPatientActivity
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null

    private val binding: ActivityHomeBinding
        get() = _binding!!

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
        var adapter = PatientListAdapter(this, dataList)

        // Set the adapter to the ListView
        binding.listView.adapter = adapter

//        binding.fab.setOnClickListener {
//            val intent = Intent(this, AddPatientActivity::class.java)
//            startActivity(intent)
//        }
    }
}