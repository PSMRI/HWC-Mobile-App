package org.piramalswasthya.cho.ui.commons.history_custom

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.MedicationStatement
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
@HiltViewModel
class HistoryCustomViewModel @Inject constructor(
    private val maleMasterDataRepository: MaleMasterDataRepository,
    private val userRepo: UserRepo,
    private val historyDao: HistoryDao,
    @ApplicationContext private val application : Context
): ViewModel(){
    private var _loggedInUser: UserCache? = null
    val loggedInUser: UserCache?
        get() = _loggedInUser

    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(application.applicationContext)

    private var _boolCall = MutableLiveData(false)
    val boolCall: LiveData<Boolean>
        get() = _boolCall
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
    fun saveIllnessORSurgeryDetailsInfo(observation: Observation){
        viewModelScope.launch {
            try{
                   var uuid = generateUuid()
                    observation.id = uuid
                    fhirEngine.create(observation)
            } catch (e: Exception){
                Timber.d("Error in Saving Illness Details Informations")
            }
        }
    }
    fun saveMedicationDetailsInfo(medicationStatement: MedicationStatement){
        viewModelScope.launch {
            try{
                var uuid = generateUuid()
                medicationStatement.id = uuid
                fhirEngine.create(medicationStatement)
                var getOI = fhirEngine.get(ResourceType.MedicationStatement,uuid)
                Log.d("Aryan","${getOI}")
            } catch (e: Exception){
                Timber.d("Error in Saving Medication Details Informations")
            }
        }
    }
    suspend fun getIllMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getIllnessByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }
    suspend fun getSurgMap(): Map<Int, String> {
        return try {
            maleMasterDataRepository.getSurgeryByNameMap()
        } catch (e: Exception) {
            Timber.d("Error in Fetching Map $e")
            emptyMap()
        }
    }
    fun saveMedicationHistoryToCache(medicationHistory: MedicationHistory){

        try{
            historyDao.insertMedicationHistory(medicationHistory)
            var obj = historyDao.getMedicationHistoryByMedicationId(medicationHistory.medicationHistoryId)
            Log.d("Aryan","${obj}")
        } catch (e: java.lang.Exception){
            Timber.d("Error in saving Surgery history $e")
        }
    }
    private fun generateUuid():String{
        return UUID.randomUUID().toString()
    }
    fun resetBool(){
        _boolCall.value = false
    }

}

//    private fun addMedicationData() {
//        val medicationRequest = MedicationRequest()
//        medicationRequest.status = MedicationRequest.MedicationRequestStatus.UNKNOWN
//        val count = binding.medicationExtra.childCount
//        val dosageInstructions = mutableListOf<Dosage>()
//
//        for (i in 0 until count) {
//            val childView: View? = binding.medicationExtra?.getChildAt(i)
//            val currentMVal = childView?.findViewById<TextInputEditText>(R.id.currentMText)?.text.toString()
//            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)?.text.toString()
//            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)?.text.toString()
//
//            val medicationHistory = MedicationHistory(
//                medicationHistoryId = "21",
//                currentMedication = currentMVal,
//                duration = durationVal,
//                durationUnit = unitDurationVal
//            )
//            viewModel.saveMedicationHistoryToCache(medicationHistory)
//        }
//    }