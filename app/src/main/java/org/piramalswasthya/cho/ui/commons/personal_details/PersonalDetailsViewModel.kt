package org.piramalswasthya.cho.ui.commons.personal_details

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.count
import com.google.android.fhir.search.search
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.RiskAssessment
import timber.log.Timber

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
@RequiresApi(Build.VERSION_CODES.O)
class PersonalDetailsViewModel(application: Application, private val fhirEngine: FhirEngine) :
        AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<PatientItem>>()
    val patientCount = MutableLiveData<Long>()

    init {
        updatePatientListAndPatientCount({ getSearchResults() }, { count() })
    }

    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount({ getSearchResults(nameQuery) }, { count(nameQuery) })
    }

    /**
     * [updatePatientListAndPatientCount] calls the search and count lambda and updates the live data
     * values accordingly. It is initially called when this [ViewModel] is created. Later its called
     * by the client every time search query changes or data-sync is completed.
     */
    private fun updatePatientListAndPatientCount(
            search: suspend () -> List<PatientItem>,
            count: suspend () -> Long
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = search()
            Log.d("sjdlkfj","Data  : ${liveSearchedPatients.value}")
            patientCount.value = count()
        }
    }

    /**
     * Returns count of all the [Patient] who match the filter criteria unlike [getSearchResults]
     * which only returns a fixed range.
     */
    private suspend fun count(nameQuery: String = ""): Long {
        return fhirEngine.count<Patient> {
            if (nameQuery.isNotEmpty()) {
                filter(
                        Patient.NAME,
                        {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        }
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getSearchResults(nameQuery: String = ""): List<PatientItem> {
        val patients: MutableList<PatientItem> = mutableListOf()
        fhirEngine
                .search<Patient> {
                    if (nameQuery.isNotEmpty()) {
                        filter(
                                Patient.NAME,
                                {
                                    modifier = StringFilterModifier.CONTAINS
                                    value = nameQuery
                                }
                        )
                    }
                    sort(Patient.GIVEN, Order.ASCENDING)
                    count = 100
                    from = 0
                }
                .mapIndexed { index, fhirPatient -> fhirPatient.toPatientItem(index + 1) }
                .let { patients.addAll(it) }
        return patients
    }

    private suspend fun getRiskAssessments(): Map<String, RiskAssessment?> {
        return fhirEngine.search<RiskAssessment> {}.groupBy { it.subject.reference }.mapValues { entry
            ->
            entry
                    .value
                    .filter { it.hasOccurrence() }
                    .sortedByDescending { it.occurrenceDateTimeType.value }
                    .firstOrNull()
        }
    }

    /** The Patient's details for display purposes. */
    data class PatientItem(
            val id: String,
            val resourceId: String,
            val name: String,
            val gender: String,
            val dob: LocalDate? = null,
            val phone: String,
            val city: String,
            val country: String,
            val isActive: Boolean,

    ) {
        override fun toString(): String = name
    }

    /** The Observation's details for display purposes. */
    data class ObservationItem(
            val id: String,
            val code: String,
            val effective: String,
            val value: String
    ) {
        override fun toString(): String = code
    }

    data class ConditionItem(
            val id: String,
            val code: String,
            val effective: String,
            val value: String
    ) {
        override fun toString(): String = code
    }

    class PatientListViewModelFactory(
            private val application: Application,
            private val fhirEngine: FhirEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PersonalDetailsViewModel::class.java)) {
                return PersonalDetailsViewModel(application, fhirEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun Patient.toPatientItem(position: Int): PersonalDetailsViewModel.PatientItem {
    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob =
            if (hasBirthDateElement())
                LocalDate.parse(birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
            else null
    val phone = if (hasTelecom()) telecom[0].value else ""
    val city = if (hasAddress()) address[0].city else ""
    val country = if (hasAddress()) address[0].country else ""
    val isActive = active

    return PersonalDetailsViewModel.PatientItem(
            id = position.toString(),
            resourceId = patientId,
            name = name,
            gender = gender ?: "",
            dob = dob,
            phone = phone ?: "",
            city = city ?: "",
            country = country ?: "",
            isActive = isActive
    )
}
