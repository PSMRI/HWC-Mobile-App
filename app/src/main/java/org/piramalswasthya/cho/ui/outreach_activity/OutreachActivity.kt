package org.piramalswasthya.cho.ui.outreach_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityOutreachBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_list.OutreachActiviityListFragmentDirections
import org.piramalswasthya.cho.utils.DateTimeUtil

@AndroidEntryPoint
class OutreachActivity : AppCompatActivity() {

    private var _binding : ActivityOutreachBinding? = null

    private val binding  : ActivityOutreachBinding
        get() = _binding!!

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOutreachBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(binding.outreachActivity.id) as NavHostFragment

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

    fun navigateToFormScreen(view: View) {
        navHostFragment.navController.navigate(
            OutreachActiviityListFragmentDirections.actionOutreachActiviityListFragmentToOutreachActivityFormFragment()
        )
    }


}