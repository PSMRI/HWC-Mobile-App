package org.piramalswasthya.cho.ui.commons.fhir_add_patient



//import org.hl7.fhir.r4.model.StructureDefinition
//import org.hl7.fhir.kotlin.FhirClient
import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.patient.patient
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import org.piramalswasthya.cho.ui.commons.ProfileLoaderImpl
import javax.inject.Inject

/** ViewModel for patient registration screen {@link AddPatientFragment}. */
@HiltViewModel
class FhirAddPatientViewModel @Inject constructor(@ApplicationContext private val application : Context, savedStateHandle: SavedStateHandle, private val service: AmritApiService) :
    ViewModel(), FhirQuestionnaireService {

    override var questionnaireJson: String? = null

    @SuppressLint("StaticFieldLeak")
    override val context: Context = application.applicationContext

    override val state = savedStateHandle

    override val isEntitySaved = MutableLiveData<Boolean>()

//    var patient : Patient = Patient()

    /**
     * Saves patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */
    override fun saveEntity(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
//            if (QuestionnaireResponseValidator.validateQuestionnaireResponse(
//                    questionnaireResource,
//                    questionnaireResponse,
//                    getApplication(application)
//                )
//                    .values
//                    .flatten()
//                    .any { it is Invalid }
//            ) {
//                isEntitySaved.value = false
//                return@launch
//            }

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse, null, ProfileLoaderImpl()).entryFirstRep
            if (entry.resource !is Patient) {
                return@launch
            }

            patient = entry.resource as Patient
            patient.id = generateUuid()
//            fhirEngine.create(patient)
            isEntitySaved.value = true
        }
    }

}
