package org.piramalswasthya.cho.ui.commons

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.json.JSONObject
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import java.util.UUID

interface FhirQuestionnaireService {

    var questionnaireJson: String?

    val context: Context

    val state : SavedStateHandle

    val isEntitySaved: MutableLiveData<Boolean>

    val questionnaire: String
        get() = getQuestionnaireJson()

    val questionnaireResource: Questionnaire
        get() = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire) as Questionnaire

    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(context)

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson = readFileFromAssets(state[FhirFragmentService.QUESTIONNAIRE_FILE_PATH_KEY]!!)

//        var questionnaireJsonObject = JSONObject(questionnaireJson!!)
//
//        questionnaireJsonObject
//            .getJSONArray("item")
//            .getJSONObject(0)
//            .getJSONArray("answerOption")
//            .getJSONObject(0)
//            .getJSONObject("valueCoding")
//            .put("display", "OPD")

        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return context.assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    fun saveEntity(questionnaireResponse: QuestionnaireResponse)

}