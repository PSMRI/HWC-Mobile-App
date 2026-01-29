package org.piramalswasthya.cho.ui.beneficiary_card.edit

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

@AndroidEntryPoint
class EditBeneficiaryDetailsActivity : AppCompatActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val preferenceDao: PreferenceDao
    }

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

        setContentView(R.layout.activity_edit_beneficiary_details)

        if (savedInstanceState == null) {
            @Suppress("DEPRECATION")
            val patientInfo = intent.getSerializableExtra(EXTRA_PATIENT_INFO) as? PatientDisplayWithVisitInfo

            patientInfo?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, EditBeneficiaryDetailsFragment.newInstance(it))
                    .commit()
            } ?: run {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as CHOApplication).activityList.remove(this)
    }

    companion object {
        const val EXTRA_PATIENT_INFO = "patientInfo"

        fun getIntent(context: Context, patientInfo: PatientDisplayWithVisitInfo): Intent {
            return Intent(context, EditBeneficiaryDetailsActivity::class.java).apply {
                putExtra(EXTRA_PATIENT_INFO, patientInfo)
            }
        }
    }
}
