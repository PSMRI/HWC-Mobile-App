package org.piramalswasthya.cho.ui.outreach_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.databinding.ActivityOutreachBinding

class OutreachActivity : AppCompatActivity() {

    private var _binding : ActivityOutreachBinding? = null

    private val binding  : ActivityOutreachBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outreach)

        val navHostFragment = supportFragmentManager.findFragmentById(binding.outreachActivity.id) as NavHostFragment

        navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.outreachActiviityListFragment -> {
                    binding.headerTextRegisterPatient.text = "Previous Activity"
                    binding.homeButton.visibility = View.VISIBLE
                }
                R.id.outreachActivityDetailsFragment -> {
                    binding.headerTextRegisterPatient.text = "Activity Details"
                    binding.homeButton.visibility = View.GONE
                }
                R.id.outreachActivityFormFragment -> {
                    binding.headerTextRegisterPatient.text = "Activity Form"
                    binding.homeButton.visibility = View.GONE
                }
            }
        }

    }


}