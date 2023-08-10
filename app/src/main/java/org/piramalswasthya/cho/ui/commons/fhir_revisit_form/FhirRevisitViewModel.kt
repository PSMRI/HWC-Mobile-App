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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StringType
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.fhir_utils.ProfileLoaderAppointment
import org.piramalswasthya.cho.fhir_utils.extension_names.createdBy
import org.piramalswasthya.cho.fhir_utils.extension_names.parkingPlaceID
import org.piramalswasthya.cho.fhir_utils.extension_names.providerServiceMapId
import org.piramalswasthya.cho.fhir_utils.extension_names.vanID
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.patient.patient
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragment
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FhirRevisitViewModel @Inject constructor(@ApplicationContext private val application : Context, savedStateHandle: SavedStateHandle, private val userDao: UserDao) :
    ViewModel(), FhirQuestionnaireService {

    override var questionnaireJson: String? = null

    @SuppressLint("StaticFieldLeak")
    override val context: Context = application.applicationContext
    private val extension: FhirExtension = FhirExtension(ResourceType.Appointment)
    private var userInfo: UserCache? = null
    override val state = savedStateHandle

    override val isEntitySaved = MutableLiveData<Boolean>()


    init{
        viewModelScope.launch {
            async { userInfo = userDao.getLoggedInUser() }.await()
        }
    }

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

            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse,null, ProfileLoaderAppointment()).entryFirstRep
            if (entry.resource !is Appointment) {
                return@launch
            }
            val appointment = entry.resource as Appointment
            appointment.id = generateUuid()
            addReferOtherDetails(appointment)
            fhirEngine.create(appointment)
            Log.d("aptId", appointment.id)
            val appointmentData = fhirEngine.get<Appointment>(appointment.id)
            val centerExtension = appointmentData.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/Appointment#Appointment.center")
            val dateExtension = appointmentData.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/Appointment#Appointment.date")
            val vanIdExtension = appointmentData.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/Appointment#Appointment.vanID")
            Log.d("appointmentDataCenter", centerExtension.value.displayString(context).toString())
            Log.d("appointmentDate", dateExtension.value.displayString(context).toString())
            Log.d("appointmentVanId", vanIdExtension.value.displayString(context).toString())
            Log.d("description", appointmentData.description)
            Log.d("service", appointmentData.serviceCategory.toString())
            isEntitySaved.value = true
        }
    }

    private fun addReferOtherDetails(appointment: Appointment){
        if(userInfo != null){
            appointment.addExtension( extension.getExtenstion(
                extension.getUrl(vanID),
                extension.getStringType(userInfo!!.vanId.toString()) ) )

            appointment.addExtension( extension.getExtenstion(
                extension.getUrl(parkingPlaceID),
                extension.getStringType(userInfo!!.parkingPlaceId.toString()) ) )

            appointment.addExtension( extension.getExtenstion(
                extension.getUrl(providerServiceMapId),
                extension.getStringType(userInfo!!.serviceMapId.toString()) ) )

            appointment.addExtension( extension.getExtenstion(
                extension.getUrl(createdBy),
                extension.getStringType(userInfo!!.userName) ) )
        }
    }


}