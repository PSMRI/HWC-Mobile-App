package org.piramalswasthya.cho.ui.commons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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
}
