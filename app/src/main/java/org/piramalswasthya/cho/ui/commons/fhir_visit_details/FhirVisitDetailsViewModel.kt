package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.model.api.annotation.Extension
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.instance.model.api.IBaseResource
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.json.JSONObject
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import timber.log.Timber
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FhirVisitDetailsViewModel @Inject
constructor(
     @ApplicationContext private val application : Context,
     private val apiService : AmritApiService,
    private val state: SavedStateHandle
     ) :
    ViewModel() {

    enum class LoadState{
        IDLE,
        LOADING,
        SUCCESS,
        FAIL
    }

    private val _state = MutableLiveData<String?>(null)
    val loadState  : LiveData<String?>
        get() = _state

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

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Patient) {
                return@launch
            }
            val patient = entry.resource as Patient
            patient.id = generateUuid()
            fhirEngine.create(patient)
            isPatientSaved.value = true

            isPatientSaved.value = true
        }
    }

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson = readFileFromAssets(state[FhirVisitDetailsFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        var questionnaireJsonObject = JSONObject(questionnaireJson!!)

//        jsonObject.getJSONArray("item")[0].get("answerOption")[0].get("valueCoding").put("display", "OPD")

        questionnaireJsonObject
            .getJSONArray("item")
            .getJSONObject(0)
            .getJSONArray("answerOption")
            .getJSONObject(0)
            .getJSONObject("valueCoding")
            .put("display", "OPD")

//        questionnaireJsonObject.put("item", emptyArray())
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

    fun launchESanjeenvani(networkBody : NetworkBody  ){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val response = apiService.getAuthRefIdForWebView(networkBody)
                    Log.d("Resp", "$response")
                    if (response != null) {
                        var referenceId = response.model.referenceId
                        var url =
                            "https://uat.esanjeevani.in/#/external-provider-signin/$referenceId"
                        Timber.d("$url")
                        _state.postValue(url)

                    }
                } catch (e: Exception) {
                    _state.postValue(null)
                    Timber.d("Not able to fetch the data due to $e")
                }
            }

        }
    }

    fun resetLoadState() {
        _state.value = null
    }

}