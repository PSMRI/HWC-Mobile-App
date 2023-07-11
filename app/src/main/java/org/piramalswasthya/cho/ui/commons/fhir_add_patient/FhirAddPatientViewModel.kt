package org.piramalswasthya.cho.ui.commons.fhir_add_patient



import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.mapping.StructureMapExtractionContext
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.ElementDefinition
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StructureDefinition
//import org.hl7.fhir.r4.model.StructureDefinition
//import org.hl7.fhir.kotlin.FhirClient
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.FhirService
import org.piramalswasthya.cho.network.TmcAuthUserRequest
import org.piramalswasthya.cho.repositories.UserAuthRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import org.piramalswasthya.cho.ui.commons.ProfileLoaderImpl
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import javax.inject.Inject

/** ViewModel for patient registration screen {@link AddPatientFragment}. */
@HiltViewModel
class FhirAddPatientViewModel @Inject constructor(@ApplicationContext private val application : Context, savedStateHandle: SavedStateHandle, private val service: AmritApiService,) :
    ViewModel(), FhirQuestionnaireService {

    override var questionnaireJson: String? = null

    @SuppressLint("StaticFieldLeak")
    override val context: Context = application.applicationContext

    override val state = savedStateHandle

    override val isEntitySaved = MutableLiveData<Boolean>()

    /**
     * Saves patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */
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

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse, null, ProfileLoaderImpl()).entryFirstRep
            if (entry.resource !is Patient) {
                return@launch
            }

            val patient = entry.resource as Patient
            patient.id = generateUuid()
            fhirEngine.create(patient)
            Log.i("patient id ", patient.id)
//            val resp = service.createPatient(patient)
//            Log.i("patient", resp.toString())
//            var pat= fhirEngine.get(ResourceType.Patient,patient.id)
//            fhirEngine.update()
            isEntitySaved.value = true
        }
    }

}
