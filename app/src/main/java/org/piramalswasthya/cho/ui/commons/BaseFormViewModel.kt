package org.piramalswasthya.cho.ui.commons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.piramalswasthya.cho.configuration.Dataset
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber

/**
 * Base ViewModel that holds UI-state boilerplate shared by all single-page
 * assessment/diagnosis form screens (State enum, showAlert, form load/save
 * state, beneficiary header LiveData).
 *
 * Subclasses should use the protected backing fields directly and call
 * [clearAlert] through the public API exposed here.
 */
abstract class BaseFormViewModel : ViewModel() {

    // ── Save / Load state ────────────────────────────────────────────────────

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    protected val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    // ── Alert message ────────────────────────────────────────────────────────

    protected val _showAlert = MutableLiveData<String?>()
    val showAlert: LiveData<String?> get() = _showAlert

    fun clearAlert() {
        _showAlert.value = null
    }

    // ── Beneficiary header info ──────────────────────────────────────────────

    protected val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    protected val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender
    var onShowAlert: ((String) -> Unit)? = null

    // ── Shared init helper ───────────────────────────────────────────────────

    /**
     * Loads the logged-in user and the patient for [patientID], populates
     * [benName] / [benAgeGender], and returns the [PatientDisplay].
     * Returns `null` (and logs an error) if either is missing.
     */
    protected suspend fun loadPatientDetails(
        userRepo: UserRepo,
        patientRepo: PatientRepo,
        patientID: String?
    ): PatientDisplay? {
        val user = userRepo.getLoggedInUser()
        if (user == null) {
            Timber.e("No logged in user found")
            return null
        }

        val patient = patientID?.let { patientRepo.getPatientDisplay(it) }
        if (patient == null) {
            Timber.e("Patient not found for ID: $patientID")
            return null
        }

        _benName.value = "${patient.patient.firstName} ${patient.patient.lastName ?: ""}"
        _benAgeGender.value =
            "${patient.patient.age} ${patient.ageUnit?.name} | ${patient.gender?.genderName}"

        return patient
    }

    /**
     * Wires [dataset].onShowAlert to forward messages into [_showAlert].
     * Call once during init after the dataset is created.
     */

    protected fun bindAlertToDataset(dataset: Dataset) {
        onShowAlert = { message ->
            Timber.d("Dataset requested alert: $message")
            _showAlert.postValue(message)
        }
    }
}
