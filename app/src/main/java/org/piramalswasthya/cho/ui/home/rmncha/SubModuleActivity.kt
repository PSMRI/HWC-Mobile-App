package org.piramalswasthya.cho.ui.home.rmncha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_MATERNAL_HEALTH
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_TYPE_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_EC_TRACKING_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ADOLESCENT_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ANC_VISITS_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ABORTION_LIST_KEY
import org.piramalswasthya.cho.databinding.ActivitySubModuleBinding

/**
 * Activity to host sub-module fragments for RMNCHA+ modules
 */
@AndroidEntryPoint
class SubModuleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubModuleBinding

    companion object {
        private const val DIRECT_FRAGMENT_KEY = "direct_fragment_key"

        fun getIntent(context: Context, moduleType: String): Intent {
            return Intent(context, SubModuleActivity::class.java).apply {
                putExtra(MODULE_TYPE_KEY, moduleType)
            }
        }

        fun getDirectFragmentIntent(context: Context, fragmentKey: String): Intent {
            return Intent(context, SubModuleActivity::class.java).apply {
                putExtra(DIRECT_FRAGMENT_KEY, fragmentKey)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val moduleType = intent.getStringExtra(MODULE_TYPE_KEY)
        val directFragmentKey = intent.getStringExtra(DIRECT_FRAGMENT_KEY)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            when {
                directFragmentKey != null -> {
                    // Direct fragment loading (EC Tracking, Adolescent List)
                    supportActionBar?.title = getDirectFragmentTitle(directFragmentKey)
                    val fragment = when (directFragmentKey) {
                        SHOW_EC_TRACKING_KEY -> org.piramalswasthya.cho.ui.home.rmncha.eligible_couple.EligibleCoupleTrackingFragment()
                        SHOW_ADOLESCENT_LIST_KEY -> org.piramalswasthya.cho.ui.home.rmncha.child_care.AdolescentListFragment()
                        SHOW_ANC_VISITS_KEY -> org.piramalswasthya.cho.ui.home.rmncha.maternal_health.ANCVisitsFragment()
                        SHOW_ABORTION_LIST_KEY -> org.piramalswasthya.cho.ui.home.rmncha.maternal_health.AbortionListFragment()
                        else -> null
                    }
                    fragment?.let {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, it)
                            .commit()
                    }
                }
                moduleType != null -> {
                    // Sub-module grid loading
                    supportActionBar?.title = getModuleTitle(moduleType)
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            SubModuleFragment.newInstance(moduleType)
                        )
                        .commit()
                }
            }
        }
    }

    private fun getModuleTitle(moduleType: String?): String {
        return when (moduleType) {
            MODULE_MATERNAL_HEALTH -> getString(R.string.maternal_health)
            else -> getString(R.string.app_name)
        }
    }

    private fun getDirectFragmentTitle(fragmentKey: String): String {
        return when (fragmentKey) {
            SHOW_EC_TRACKING_KEY -> getString(R.string.eligible_couple_tracking)
            SHOW_ADOLESCENT_LIST_KEY -> getString(R.string.adolescent_list)
            SHOW_ANC_VISITS_KEY -> getString(R.string.anc_visits)
            SHOW_ABORTION_LIST_KEY -> getString(R.string.abortion_list)
            else -> getString(R.string.app_name)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
