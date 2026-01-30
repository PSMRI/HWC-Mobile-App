package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityDeliveryOutcomeFormBinding
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form.DeliveryOutcomeFormFragment

/**
 * Activity to record maternal condition, complications, and admission status.
 * Used when opened from Delivery Outcome list (no visit context).
 * On Next, saves and finishes.
 */
@AndroidEntryPoint
class DeliveryOutcomeFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDeliveryOutcomeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbarWithBack(binding.toolbar, getString(R.string.delivery_outcome))

        val patientID = intent.getStringExtra(EXTRA_PATIENT_ID) ?: return finish()
        val visitNumber = intent.getIntExtra(EXTRA_VISIT_NUMBER, 1)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(
                    R.id.delivery_outcome_form_container,
                    DeliveryOutcomeFormFragment::class.java,
                    Bundle().apply {
                        putString("patientID", patientID)
                        putInt("visitNumber", visitNumber)
                    }
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val EXTRA_PATIENT_ID = "patientID"
        private const val EXTRA_VISIT_NUMBER = "visitNumber"

        fun getIntent(context: Context, patientID: String, visitNumber: Int = 1): Intent {
            return Intent(context, DeliveryOutcomeFormActivity::class.java).apply {
                putExtra(EXTRA_PATIENT_ID, patientID)
                putExtra(EXTRA_VISIT_NUMBER, visitNumber)
            }
        }
    }
}
