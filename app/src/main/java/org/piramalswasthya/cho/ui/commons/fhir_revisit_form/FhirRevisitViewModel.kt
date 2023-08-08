package org.piramalswasthya.cho.ui.commons.fhir_revisit_form

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
import com.google.android.fhir.datacapture.common.datatype.displayString
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.google.android.fhir.get
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StringType
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import org.piramalswasthya.cho.ui.commons.ProfileLoaderImpl
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragment
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FhirRevisitViewModel @Inject constructor(@ApplicationContext private val application : Context, savedStateHandle: SavedStateHandle) :
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

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse,null, ProfileLoaderImpl()).entryFirstRep
            if (entry.resource !is Appointment) {
                return@launch
            }
            val appointment = entry.resource as Appointment
            appointment.id = generateUuid()
            fhirEngine.create(appointment)

            Log.d("aptId", appointment.id)
            val appointmentData = fhirEngine.get<Appointment>(appointment.id)
            val centerExtension = appointmentData.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/Appointment#Appointment.center")
            val dateExtension = appointmentData.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/Appointment#Appointment.date")
            Log.d("appointmentDataCenter", centerExtension.value.displayString(context).toString())
            Log.d("appointmentDate", dateExtension.value.displayString(context).toString())
            Log.d("description", appointmentData.description)
            Log.d("service", appointmentData.serviceCategory.toString())

            isEntitySaved.value = true
        }
    }


}