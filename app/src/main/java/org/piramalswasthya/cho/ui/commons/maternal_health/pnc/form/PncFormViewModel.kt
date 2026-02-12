package org.piramalswasthya.cho.ui.commons.maternal_health.pnc.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.configuration.PncFormDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.PncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.R
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PncFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val pncRepo: PncRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }


    private val patientID =
        PncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID
    private val visitNumber =
        PncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber


    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    //    private lateinit var user: UserDomain
    private val dataset =
        PncFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var pncCache: PNCVisitCache
    var deliveryOutcome: DeliveryOutcomeCache? = null

    suspend fun hasPreviousPermanentSterilization(): Boolean {
        return pncRepo.getAllPNCsByPatId(patientID)
            .filter { it.pncPeriod.toInt() < visitNumber.toInt() }
            .any { pncVisit ->
                pncVisit.contraceptionMethod?.let { method ->
                    isPermanentSterilizationMethod(method)
                } ?: false
            }
    }

    suspend fun getLastPermanentSterilizationVisit(
        currentVisitNumber: Int
    ): PNCVisitCache? {
        return pncRepo.getAllPNCsByPatId(patientID)
            .filter { it.pncPeriod < currentVisitNumber }
            .filter { pncVisit ->
                pncVisit.contraceptionMethod?.let { method ->
                    isPermanentSterilizationMethod(method)
                } ?: false
            }
            .maxByOrNull { it.pncPeriod }
    }

    private fun isPermanentSterilizationMethod(method: String): Boolean {
        val permanentMethods = context.resources.getStringArray(R.array.sterilization_methods_array).toList()
        return permanentMethods.any { it.equals(method, ignoreCase = true) }
    }

    init {
        viewModelScope.launch {
            val asha = userRepo.getLoggedInUser()!!
            val ben = patientRepo.getPatientDisplay(patientID)?.also { ben ->
                _benName.value =
                    "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"
                pncCache = PNCVisitCache(
                    patientID = patientID,
                    pncPeriod = visitNumber,
                    isActive = true,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }
            deliveryOutcome = deliveryOutcomeRepo.getDeliveryOutcome(patientID)
            if (deliveryOutcome == null) {
                Timber.e("Delivery outcome not found for patient $patientID")
                _state.postValue(State.SAVE_FAILED)
                return@launch
            }
            
            pncRepo.getSavedPncRecord(patientID, visitNumber)?.let {
                pncCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }
            val lastPnc = pncRepo.getLastFilledPncRecord(patientID)
            val hasPreviousSterilization = hasPreviousPermanentSterilization()
            val lastSterilizationVisit = if (hasPreviousSterilization) {
                getLastPermanentSterilizationVisit(visitNumber)
            } else null

            dataset.setUpPage(
                visitNumber,
                ben,
                deliveryOutcome!!,
                lastPnc,
                if (recordExists.value == true) pncCache else null,
                hasPreviousSterilization,
                lastSterilizationVisit
            )
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }


    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(pncCache, 1)
                    pncRepo.persistPncRecord(pncCache)

                    // Update delivery outcome if date of delivery is missing
                    if (deliveryOutcome?.dateOfDelivery == null || deliveryOutcome?.dateOfDelivery == 0L) {
                        val saveDeliveryOutcome = DeliveryOutcomeCache(
                            patientID = patientID,
                            syncState = SyncState.UNSYNCED,
                            createdBy = pncCache.updatedBy,
                            updatedBy = pncCache.updatedBy,
                            dateOfDelivery = pncCache.pncDate, // Use PNC date as fallback
                            isActive = true
                        )
                        deliveryOutcomeRepo.saveDeliveryOutcome(saveDeliveryOutcome)
                    }

                    // Update woman status after PNC
                    updateWomanStatusAfterPnc(pncCache)

                    // Handle maternal death
                    handleMaternalDeath(pncCache)

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PNC data failed!! $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    // ─── Helper: close PNC visits and update patient on maternal death ───
    private suspend fun handleMaternalDeath(pncCache: PNCVisitCache) {
        if (!pncCache.motherDeath) return
        val patient = patientRepo.getPatient(patientID)

        // Close PNC case immediately - mark all active PNC visits as inactive
        val allPncVisits = pncRepo.getAllPNCsByPatId(patientID)
        allPncVisits.forEach { visit ->
            visit.isActive = false
            visit.syncState = SyncState.UNSYNCED
            if (visit.processed != "N") visit.processed = "U"
            visit.updatedDate = System.currentTimeMillis()
            visit.updatedBy = pncCache.updatedBy
            pncRepo.persistPncRecord(visit)
        }

        // Update beneficiary status = Death
        // Note: Patient model may need to be updated to include death status field
        // For now, we'll sync the death information
        patient.syncState = SyncState.UNSYNCED
        patientRepo.updateRecord(patient)
        // Sync death details to AMRIT (handled by sync worker)
    }

    private suspend fun updateWomanStatusAfterPnc(pncCache: PNCVisitCache) {
        // Skip status update if maternal death occurred
        if (pncCache.motherDeath) {
            return
        }
        
        val patient = patientRepo.getPatient(patientID)
        val dateOfDelivery = deliveryOutcome?.dateOfDelivery ?: return
        
        val is42ndDayPnc = pncCache.pncPeriod == 42
        val daysSinceDelivery = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - dateOfDelivery)
        val isAfter60Days = daysSinceDelivery >= 60

        // Transition to Eligible Couple Tracking
        // After 42nd day PNC visit submission OR 60 days from Date of Delivery
        if (is42ndDayPnc || isAfter60Days) {
            val allPncVisits = pncRepo.getAllPNCsByPatId(patientID)
            val permanentSterilizationMethods = context.resources.getStringArray(R.array.sterilization_methods_array).toList()
            
            val hasPermanentSterilization = allPncVisits.any { pncVisit ->
                pncVisit.contraceptionMethod?.let { method ->
                    permanentSterilizationMethods.any { sterilizationMethod ->
                        method.contains(sterilizationMethod, ignoreCase = true)
                    }
                } ?: false
            }

            // Update patient status based on sterilization
            // Note: This may need to be adapted based on HWC's Patient model structure
            // The Patient model in HWC may not have reproductiveStatus field directly
            // Status updates might need to be handled through a different mechanism
            if (hasPermanentSterilization) {
                // If female permanent method selected:
                // After 42nd day PNC submission OR 60 days from delivery:
                // Update Status of Woman = Permanently Sterilized
                val femaleSterilizationMethods = context.resources.getStringArray(R.array.female_sterilization_methods_array).toList()
                val hasFemalePermanentSterilization = allPncVisits.any { pncVisit ->
                    pncVisit.contraceptionMethod?.let { method ->
                        femaleSterilizationMethods.any { sterilizationMethod ->
                            method.contains(sterilizationMethod, ignoreCase = true)
                        }
                    } ?: false
                }
                
                if (hasFemalePermanentSterilization) {
                    // Update to Permanently Sterilized status
                    // patient.reproductiveStatus = "Permanently Sterilised"
                    // Move record to Eligible Couple and ECT Tracking sections
                }
            } else {
                // If no permanent sterilization selected:
                // Update Status of Woman = Eligible Couple
                // Move record to Eligible Couple and ECT Tracking sections
                // patient.reproductiveStatus = "Eligible Couple"
            }
            
            patient.syncState = SyncState.UNSYNCED
            patientRepo.updateRecord(patient)
        }
    }

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b

    }

}