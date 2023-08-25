package org.piramalswasthya.cho.ui.commons.fhir_patient_vitals

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.google.android.fhir.get
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FhirVitalsViewModel @Inject constructor(@ApplicationContext private val application : Context,
                                              savedStateHandle: SavedStateHandle,
//                                             private var apiInterface: ESanjeevaniApiService,
                                              private val userRepo: UserRepo,
                                              private val vitalsRepo: VitalsRepo,
) :
    ViewModel(), FhirQuestionnaireService {
    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser
    private var _boolCall = MutableLiveData(false)
    val boolCall: LiveData<Boolean>
        get() = _boolCall
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
            if (entry.resource !is Observation) {
                return@launch
            }
            val vitalsObservation = entry.resource as Observation
            vitalsObservation.id = generateUuid()
            fhirEngine.create(vitalsObservation)

            val vitalsObservationData = fhirEngine.get<Observation>(vitalsObservation.id)
            Log.d("data", vitalsObservationData.valueStringType.toString())
            isEntitySaved.value = true
        }
    }
    fun getLoggedInUserDetails(){
        viewModelScope.launch {
            try {
                _loggedInUser = userRepo.getUserCacheDetails()
                _boolCall.value = true
            } catch (e: Exception){
                Timber.d("Error in calling getLoggedInUserDetails() $e")
                _boolCall.value = false
            }
        }
    }
    fun resetBool(){
        _boolCall.value = false
    }
    fun saveObservationResource(observation: Observation){
        viewModelScope.launch {
            try{
                    var uuid = generateUuid()
                    observation.id = uuid
                    fhirEngine.create(observation)
                    Toast.makeText(context, context.getString(R.string.vitals_information_is_saved), Toast.LENGTH_SHORT).show()
                // Serialize the Observation resource to JSON
                    val context = FhirContext.forR4()
                    val jsonBody = context.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation)
                    val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody)
                    var getobs = fhirEngine.get(ResourceType.Observation,uuid)
                Log.d("VitalsJson", jsonBody)

            } catch (e: Exception){
                Timber.d("Error in Saving Vitals Information")
            }
        }
    }

    fun savePatientVitalInfoToCache(patientVitalsModel: PatientVitalsModel){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    vitalsRepo.saveVitalsInfoToCache(patientVitalsModel)
                }
            } catch (e: Exception) {
                Timber.e("Error in saving vitals information : $e")
            }
        }
    }
}