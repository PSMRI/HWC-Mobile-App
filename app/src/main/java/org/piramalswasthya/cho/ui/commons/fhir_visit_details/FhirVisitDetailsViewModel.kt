package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.model.api.annotation.Extension
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import kotlinx.coroutines.launch
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.json.JSONObject
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import timber.log.Timber
import java.util.UUID

class FhirVisitDetailsViewModel (application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    val questionnaire: String
        get() = getQuestionnaireJson()
    val isPatientSaved = MutableLiveData<Boolean>()


    private val questionnaireResource: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
                    as Questionnaire
    private var fhirEngine: FhirEngine = CHOApplication.fhirEngine(application.applicationContext)

    private var questionnaireJson: String? = null

    /**
     * Saves patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */
    fun savePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            Log.i("before launch", "launch")
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
                Log.i("launch1", "launch1")
                return@launch
            }

            val response = questionnaireResponse
//            val entry =
//                ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
////            if (entry.resource !is Patient) {
//                Log.i("launch2", "launch2")
//                return@launch
//            }

//            var patient = entry.resource as QuestionnaireResponse
            questionnaireResponse.id = generateUuid()
            val ids = fhirEngine.create(questionnaireResponse)
            val patient1 = fhirEngine.get(questionnaireResponse.resourceType, questionnaireResponse.id)


            Log.i("patient", patient1.toString())
            isPatientSaved.value = true
        }
    }

//  "extension": [
//    {
//      "url": "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-itemExtractionContext",
//      "valueExpression": {
//        "language": "application/x-fhir-query",
//        "expression": "Questionnaire",
//        "name": "patient"
//      }
//    }
//  ],

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson = readFileFromAssets(state[FhirVisitDetailsFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        var questionnaireJsonObject = JSONObject(questionnaireJson!!)

//        questionnaireJsonObject
//            .getJSONArray("item")
//            .getJSONObject(0)
//            .getJSONArray("answerOption")
//            .getJSONObject(0)
//            .getJSONObject("valueCoding")
//            .put("display", "OPD")

        return questionnaireJsonObject.toString()
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

}