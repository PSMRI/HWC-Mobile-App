package org.piramalswasthya.cho.ui.home.rmncha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_CHILD_CARE
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_ELIGIBLE_COUPLE
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_MATERNAL_HEALTH
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_TYPE_KEY
import org.piramalswasthya.cho.databinding.ActivitySubModuleBinding

/**
 * Activity to host sub-module fragments for RMNCHA+ modules
 */
@AndroidEntryPoint
class SubModuleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubModuleBinding

    companion object {
        fun getIntent(context: Context, moduleType: String): Intent {
            return Intent(context, SubModuleActivity::class.java).apply {
                putExtra(MODULE_TYPE_KEY, moduleType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val moduleType = intent.getStringExtra(MODULE_TYPE_KEY)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getModuleTitle(moduleType)

        // Load SubModuleFragment if not already loaded
        if (savedInstanceState == null && moduleType != null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    SubModuleFragment.newInstance(moduleType)
                )
                .commit()
        }
    }

    private fun getModuleTitle(moduleType: String?): String {
        return when (moduleType) {
            MODULE_MATERNAL_HEALTH -> getString(R.string.maternal_health)
            MODULE_CHILD_CARE -> getString(R.string.child_care)
            MODULE_ELIGIBLE_COUPLE -> getString(R.string.eligible_couple_list)
            else -> getString(R.string.app_name)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
