package org.piramalswasthya.cho.ui.beneficiary_card

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.helpers.MyContextWrapper
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity

@AndroidEntryPoint
class BeneficiaryCardActivity : AppCompatActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val preferenceDao: PreferenceDao
    }

    private var patientInfo: PatientDisplayWithVisitInfo? = null
    private var statusOfWomanID: Int? = null

    override fun attachBaseContext(newBase: Context) {
        val pref = EntryPointAccessors.fromApplication(
            newBase,
            WrapperEntryPoint::class.java
        ).preferenceDao
        super.attachBaseContext(
            MyContextWrapper.wrap(
                newBase,
                newBase.applicationContext,
                pref.getCurrentLanguage().symbol
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as CHOApplication).addActivity(this)

        setContentView(R.layout.activity_beneficiary_card)

        statusOfWomanID = intent.getIntExtra(EXTRA_STATUS_OF_WOMAN_ID, -1).takeIf { it != -1 }

        if (savedInstanceState == null) {
            @Suppress("DEPRECATION")
            patientInfo = intent.getSerializableExtra(EXTRA_PATIENT_INFO) as? PatientDisplayWithVisitInfo

            patientInfo?.let {
                val fragment = BeneficiaryCardFragment.newInstance(it)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            } ?: run {
                // No patient info provided, finish the activity
                finish()
            }
        }
    }

    
    fun navigateToNextScreen() {
        patientInfo?.let { patient ->
            val intent = Intent(this, EditPatientDetailsActivity::class.java)
            intent.putExtra("benVisitInfo", patient)

            when (statusOfWomanID) {
                1 -> {
                    // EC - Navigate to Eligible Couple Tracking
                    intent.putExtra("navigateToEC", true)
                }
                2 -> {
                    // PW - Navigate to ANC/Pregnancy Module
                    intent.putExtra("navigateToPW", true)
                }
                3 -> {
                    // Postnatal - Navigate to PNC Module
                    intent.putExtra("navigateToPN", true)
                }
            }

            startActivity(intent)
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as CHOApplication).activityList.remove(this)
    }

    companion object {
        const val EXTRA_PATIENT_INFO = "patientInfo"
        const val EXTRA_STATUS_OF_WOMAN_ID = "statusOfWomanID"

        fun getIntent(context: Context, patientInfo: PatientDisplayWithVisitInfo): Intent {
            return Intent(context, BeneficiaryCardActivity::class.java).apply {
                putExtra(EXTRA_PATIENT_INFO, patientInfo)
            }
        }
    }
}
