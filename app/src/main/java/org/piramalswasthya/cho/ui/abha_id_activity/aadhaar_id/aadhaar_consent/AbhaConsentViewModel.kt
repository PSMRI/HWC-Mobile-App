package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.aadhaar_consent

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import javax.inject.Inject

@HiltViewModel
class AbhaConsentViewModel  @Inject constructor(private val pref: PreferenceDao) : ViewModel() {

    val currentUser = pref.getLoggedInUser()



}