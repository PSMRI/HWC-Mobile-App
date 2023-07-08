package org.piramalswasthya.cho.ui.commons

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import org.hl7.fhir.r4.model.Questionnaire
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import java.util.UUID

interface FhirQuestionnaireService {

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
    }

    var questionnaireJson: String?

    val context: Context

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
        questionnaireJson = readFileFromAssets(QUESTIONNAIRE_FILE_PATH_KEY)
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

}