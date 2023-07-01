package org.piramalswasthya.cho.ui.commons.fhir_add_patient



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.json.JSONArray
import org.json.JSONObject
import org.piramalswasthya.cho.CHOApplication
import java.util.UUID


/** ViewModel for patient registration screen {@link AddPatientFragment}. */
class FhirAddPatientViewModel(application: Application, private val state: SavedStateHandle) :
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

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Patient) {
                return@launch
            }
            val patient = entry.resource as Patient
            patient.id = generateUuid()
            fhirEngine.create(patient)
//            patient.
//            val patient1 = fhirEngine.get(Patient::class.java, patient.id)
            fhirEngine.get(patient.resourceType, patient.id )
            fhirEngine.update()
            isPatientSaved.value = true
        }
    }

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson = readFileFromAssets(state[FhirAddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        var questionnaireJsonObject = JSONObject(questionnaireJson!!)

//        jsonObject.getJSONArray("item")[0].get("answerOption")[0].get("valueCoding").put("display", "OPD")
//        questionnaireJsonObject
//            .getJSONArray("item")
//            .getJSONObject(0)
//            .getJSONArray("answerOption")
//            .getJSONObject(0)
//            .getJSONObject("valueCoding")
//            .put("display", "OPD")
//        itemArray.getJSONObject(0).getJSONArray("")


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
