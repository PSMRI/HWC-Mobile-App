package org.piramalswasthya.cho.ui.commons.fhir_revisit_form

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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.fhir_utils.ProfileLoaderAppointment
import org.piramalswasthya.cho.fhir_utils.extension_names.benFlowID
import org.piramalswasthya.cho.fhir_utils.extension_names.beneficiaryID
import org.piramalswasthya.cho.fhir_utils.extension_names.beneficiaryRegID
import org.piramalswasthya.cho.fhir_utils.extension_names.createdBy
import org.piramalswasthya.cho.fhir_utils.extension_names.parkingPlaceID
import org.piramalswasthya.cho.fhir_utils.extension_names.providerServiceMapId
import org.piramalswasthya.cho.fhir_utils.extension_names.vanID
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.model.ReferRevisitModel
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.ReferRevisitRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FhirRevisitViewModel @Inject constructor(@ApplicationContext private val application : Context,
                                               savedStateHandle: SavedStateHandle,
                                               private val userDao: UserDao,
                                               private val userRepo: UserRepo,
                                               private val doctorMasterDataMaleRepo: DoctorMasterDataMaleRepo,
                                               private val revisitRepo: ReferRevisitRepo,) :
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
    private val extension: FhirExtension = FhirExtension(ResourceType.Appointment)
    private var userInfo: UserCache? = null
    override val state = savedStateHandle

    override val isEntitySaved = MutableLiveData<Boolean>()
    private var _higherHealthCenterDropdown: LiveData<List<HigherHealthCenter>>

    val higherHealthCenterDropdown: LiveData<List<HigherHealthCenter>>
        get() = _higherHealthCenterDropdown

    init{
        viewModelScope.launch {
            async { userInfo = userDao.getLoggedInUser() }.await()
        }
        _higherHealthCenterDropdown = MutableLiveData()
        getHigherHealthCenterDropdown()
    }



    private fun getHigherHealthCenterDropdown(){
        try{
            _higherHealthCenterDropdown  = doctorMasterDataMaleRepo.getHigherHealthCenter()

        } catch (e: java.lang.Exception){
            Timber.d("Error in fetching Health Center List $e")
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
//            addReferOtherDetails(appointment)
            fhirEngine.create(appointment)
            Log.d("aptId", appointment.id)
            val appointmentData = fhirEngine.get<Appointment>(appointment.id)

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
        //TODO: Add the following Ids when available
        appointment.addExtension( extension.getExtenstion(
            extension.getUrl(beneficiaryID),
            extension.getStringType("") ) )
        appointment.addExtension( extension.getExtenstion(
            extension.getUrl(beneficiaryRegID),
            extension.getStringType("") ))
        appointment.addExtension( extension.getExtenstion(
            extension.getUrl(benFlowID),
            extension.getStringType("") ) )
    }
    fun saveAppointmentResource(appointment: Appointment){
        viewModelScope.launch {
            try{
                var uuid = generateUuid()
                appointment.id = uuid
                fhirEngine.create(appointment)
//                Toast.makeText(context, "Appointment resource is saved!", Toast.LENGTH_SHORT).show()
                // Serialize the Observation resource to JSON
                    val context = FhirContext.forR4()
                    val jsonBody = context.newJsonParser().setPrettyPrint(true).encodeResourceToString(appointment)
                    val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody)
                Log.d("ReferJson", jsonBody)
//                    apiInterface.saveObs(requestBody)
                var getAppointment = fhirEngine.get(ResourceType.Appointment,uuid)
            } catch (e: Exception){
                Timber.d(context.getString(R.string.error_in_saving_refer_and_revisit_information))
            }
        }
    }
    fun saveReferInfoToCache(referRevisitModel: ReferRevisitModel){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    revisitRepo.saveReferInfoToCache(referRevisitModel)
                    Toast.makeText(context,context.getString(R.string.saved), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Timber.d(context.getString(R.string.error_in_saving_refer_and_revisit_information))
            }
        }
    }
    fun getLoggedInUserDetails(){
        viewModelScope.launch {
            try {
                _loggedInUser = userRepo.getUserCacheDetails()
                _boolCall.value = true
            } catch (e: Exception){
                Timber.d(context.getString(R.string.error))
                _boolCall.value = false
            }
        }
    }
    fun resetBool(){
        _boolCall.value = false
    }

}