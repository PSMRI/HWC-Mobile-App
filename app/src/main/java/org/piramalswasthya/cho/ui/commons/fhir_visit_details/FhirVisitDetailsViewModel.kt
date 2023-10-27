package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class FhirVisitDetailsViewModel @Inject constructor(@ApplicationContext private val application : Context, savedStateHandle: SavedStateHandle) :
    ViewModel() {

    @SuppressLint("StaticFieldLeak")
    val context: Context = application.applicationContext

    val state = savedStateHandle

    /**
     * Saves patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */

}