package org.piramalswasthya.cho.ui.commons.fhir_examination_form

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FhirExaminationFormViewModel @Inject constructor(@ApplicationContext private val application : Context, savedStateHandle: SavedStateHandle, private val service: AmritApiService,) :
    ViewModel(), FhirQuestionnaireService {

    override var questionnaireJson: String? = null

    @SuppressLint("StaticFieldLeak")
    override val context: Context = application.applicationContext

    override val state = savedStateHandle

    override val isEntitySaved = MutableLiveData<Boolean>()

    override fun saveEntity(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            if (QuestionnaireResponseValidator.validateQuestionnaireResponse(
                    questionnaireResource,
                    questionnaireResponse,
                    getApplication(application)
                )
                    .values
                    .flatten()
                    .any { it is Invalid }
            ) {
                isEntitySaved.value = false
                return@launch
            }

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Patient) {
                return@launch
            }
            val patient = entry.resource as Patient
            patient.id = generateUuid()
            fhirEngine.create(patient)
            isEntitySaved.value = true
        }
    }

}