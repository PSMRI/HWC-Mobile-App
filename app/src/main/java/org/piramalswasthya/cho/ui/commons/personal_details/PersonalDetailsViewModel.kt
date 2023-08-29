package org.piramalswasthya.cho.ui.commons.personal_details

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.count
import com.google.android.fhir.search.search
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient

import org.piramalswasthya.cho.model.Patient as patient
import org.hl7.fhir.r4.model.RiskAssessment
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.ui.register_patient_activity.location_details.LocationViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */

@HiltViewModel
class PersonalDetailsViewModel @Inject constructor(
    private val patientRepo: PatientRepo,
) : ViewModel() {

    var patientList = listOf<PatientDisplay>()

    enum class NetworkState {
        IDLE,
        LOADING,
        SUCCESS,
        FAILURE
    }

    private val _patientObserver = MutableLiveData(NetworkState.IDLE)
    val patientObserver: LiveData<NetworkState>
        get() = _patientObserver

    init {
        getPatientList()
    }

    fun getPatientList() {
        viewModelScope.launch {
            _patientObserver.value = NetworkState.LOADING
            patientList = patientRepo.getPatientList();
            _patientObserver.value = NetworkState.SUCCESS
        }
    }

}
