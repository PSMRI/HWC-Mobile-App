package org.piramalswasthya.cho.ui.commons.fhir_add_patient



import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.mapping.StructureMapExtractionContext
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import dagger.hilt.android.lifecycle.HiltViewModel
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
class FhirAddPatientViewModel @Inject constructor(application: Application, private val state: SavedStateHandle, private val service: AmritApiService,) :
    AndroidViewModel(application), FhirQuestionnaireService {

//    val questionnaire: String
//        get() = getQuestionnaireJson()

    val isPatientSaved = MutableLiveData<Boolean>()

    override var questionnaireJson: String? = null

    @SuppressLint("StaticFieldLeak")
    override val context: Context = application.applicationContext

//
//    private val questionnaireResource: Questionnaire
//        get() =
//            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
//                    as Questionnaire
//    private var fhirEngine: FhirEngine = CHOApplication.fhirEngine(application.applicationContext)
//    private var questionnaireJson: String? = null

    /**
     * Saves patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */
    fun savePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            if (QuestionnaireResponseValidator.validateQuestionnaireResponse(
                    questionnaireResource,
                    questionnaireResponse,
                    getApplication()
                )
                    .values
                    .flatten()
                    .any { it is Invalid }
            ) {
                isPatientSaved.value = false
                return@launch
            }

//            StructureMapExtractionContext
//            StructureDefinition
//
//
//            val fhirContext: FhirContext = FhirContext.forR4()
//
//            // Create a FHIR client
//            val client = fhirContext.newRestfulGenericClient("http://example.com/fhir")
//
//            // Search for StructureDefinition resources matching the provided URL
//            val bundle: Bundle = client.createSearchRequest<StructureDefinition>()
//
//            // Retrieve the first StructureDefinition resource from the bundle
//            if (bundle.total > 0 && bundle.entry.size > 0) {
//                val structureDefinition = bundle.entry[0].resource as StructureDefinition
//
//            }
//
//
//
//            val client = FhirClient()
//            val request = client.createSearchRequest<StructureDefinition>()
//            request.setUrl("http://hl7.org/fhir/StructureDefinition/\"http://hl7.org/fhir/StructureDefinition/patient\"")
//            val response = client.execute(request)
//            return response.firstResult()

//            val fhirContext: FhirContext = FhirContext.forR4()
//
////            val canonicalType = CanonicalType("http://hl7.org/fhir/StructureDefinition/Patient")
//            val structureDefinition = fhirContext.("http://hl7.org/fhir/StructureDefinition/Patient")
//            Log.i("tag", structureDefinition.getResourceProfile())

//            val pat = Patient;
//
//            Log.i("", )

//            val elementDefinition = ElementDefinition()
//            elementDefinition.path = "Patient.extension"
//            val structureDefinition = StructureDefinition()
//            structureDefinition.snapshot.element.add(elementDefinition)



            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse, null, ProfileLoaderImpl()).entryFirstRep
            if (entry.resource !is Patient) {
                return@launch
            }

            val patient = entry.resource as Patient
            Log.i("fhir type", patient.fhirType())
            patient.id = generateUuid()

            fhirEngine.create(patient)

//            val resp = service.createPatient(patient)
//            Log.i("patient", resp.toString())
            var pat= fhirEngine.get(ResourceType.Patient,patient.id)
            fhirEngine.update()
            isPatientSaved.value = true
        }
    }

//    private fun getQuestionnaireJson(): String {
//        questionnaireJson?.let {
//            return it
//        }
//        questionnaireJson = readFileFromAssets(state[FhirAddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
//        return questionnaireJson!!
//    }
//
//    private fun readFileFromAssets(filename: String): String {
//        return getApplication<Application>().assets.open(filename).bufferedReader().use {
//            it.readText()
//        }
//    }


}
