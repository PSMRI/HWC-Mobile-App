package org.piramalswasthya.cho.ui.commons

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity-scoped holder for CPHC assessment data collected on form "Next".
 * Persistence and push sync run only when the visit is finally submitted
 * (e.g. "Submit to Doctor" on vitals), not when navigating past the form.
 */
@HiltViewModel
class PendingCphcFormViewModel @Inject constructor() : ViewModel() {

    private var persistAction: (suspend () -> Unit)? = null
    private var enqueuePush: ((Context) -> Unit)? = null

    fun stage(persist: suspend () -> Unit, enqueuePush: (Context) -> Unit) {
        persistAction = persist
        this.enqueuePush = enqueuePush
    }

    fun hasPending(): Boolean = persistAction != null

    suspend fun persistPending(context: Context) {
        val persist = persistAction ?: return
        val push = enqueuePush
        try {
            persist()
            push?.invoke(context)
        } catch (e: Exception) {
            Timber.e(e, "Failed to persist pending CPHC form")
            throw e
        } finally {
            clear()
        }
    }

    fun clear() {
        persistAction = null
        enqueuePush = null
    }

    override fun onCleared() {
        clear()
        super.onCleared()
    }
}
