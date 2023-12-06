package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.form.ImmunizationFormFragmentArgs
import javax.inject.Inject

@HiltViewModel
class OutreachActivityDetailsViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    // TODO: Implement the ViewModel
}