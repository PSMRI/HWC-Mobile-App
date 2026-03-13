package org.piramalswasthya.cho.ui.commons.maternal_health.neonatal_outcome.form

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
import org.piramalswasthya.cho.model.NeonatalOutcomeCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.NeonatalOutcomeRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.configuration.NeonatalOutcomeDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NeonatalOutcomeFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val neonatalOutcomeRepo: NeonatalOutcomeRepo,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao,
) : ViewModel() {

    private val args = NeonatalOutcomeFormFragmentArgs.fromSavedStateHandle(savedStateHandle)
    val deliveryOutcomeId: Long = args.deliveryOutcomeId
    val patientID: String = args.patientID
    val babyIndex: Int = args.babyIndex

    sealed class State {
        object IDLE : State()
        object LOADING : State()
        object SAVING : State()
        object SAVE_SUCCESS : State()
        object SAVE_FAILED : State()
    }

    private val _state = MutableLiveData<State>(State.IDLE)
    val state: LiveData<State> get() = _state

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val dataset = NeonatalOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formElements = dataset.listFlow
    val alertMessageFlow = dataset.alertErrorMessageFlow
    
    // We only need one outcome for the specific babyIndex
    private var currentOutcome: NeonatalOutcomeCache? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = State.LOADING
                
                // Get delivery outcome to verify
                val deliveryOutcome = withContext(Dispatchers.IO) {
                    deliveryOutcomeRepo.getDeliveryOutcome(patientID)
                }

                if (deliveryOutcome == null) {
                    _errorMessage.value = "Delivery outcome not found"
                    _state.value = State.SAVE_FAILED
                    return@launch
                }

                // Load existing neonatal outcome for this specific baby index
                val existing = withContext(Dispatchers.IO) {
                    neonatalOutcomeRepo.getNeonatalOutcomeByIndexAndDelivery(deliveryOutcomeId, babyIndex)
                }

                val user = userRepo.getLoggedInUser()
                val userName = user?.userName ?: ""
                
                currentOutcome = existing ?: NeonatalOutcomeCache(
                    deliveryOutcomeId = deliveryOutcomeId,
                    neonateIndex = babyIndex,
                    createdBy = userName,
                    updatedBy = userName,
                    syncState = SyncState.UNSYNCED
                )

                dataset.setUpPage(currentOutcome)
                _state.value = State.IDLE
            } catch (e: Exception) {
                Timber.e(e, "Error loading neonatal outcome data")
                _errorMessage.value = "Error loading data: ${e.message}"
                _state.value = State.SAVE_FAILED
            }
        }
    }

    fun onFieldValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun saveForm() {
        val outcome = currentOutcome ?: return
        viewModelScope.launch {
            try {
                _state.value = State.SAVING
                
                dataset.mapValues(outcome)
                
                val user = userRepo.getLoggedInUser()
                val userName = user?.userName ?: ""
                val currentTime = System.currentTimeMillis()

                val updated = outcome.copy(
                    updatedBy = userName,
                    updatedDate = currentTime,
                    syncState = SyncState.UNSYNCED
                )

                withContext(Dispatchers.IO) {
                    if (updated.id == 0L) {
                        neonatalOutcomeRepo.insertNeonatalOutcome(updated)
                    } else {
                        neonatalOutcomeRepo.updateNeonatalOutcome(updated)
                    }
                }

                _state.value = State.SAVE_SUCCESS
            } catch (e: Exception) {
                Timber.e(e, "Failed to save neonatal outcome")
                _errorMessage.value = "Failed to save: ${e.message}"
                _state.value = State.SAVE_FAILED
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
