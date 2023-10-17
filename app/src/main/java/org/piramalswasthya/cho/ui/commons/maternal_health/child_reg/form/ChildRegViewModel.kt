package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.child_reg.form

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
import org.piramalswasthya.sakhi.configuration.ChildRegistrationDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.ChildRegRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChildRegViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val childRegRepo: ChildRegRepo,
    private val infantRegRepo: InfantRegRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val motherBenId =
        ChildRegFragmentArgs.fromSavedStateHandle(savedStateHandle).motherBenId
    val babyIndex =
        ChildRegFragmentArgs.fromSavedStateHandle(savedStateHandle).babyIndex

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _recordExists = MutableLiveData(false)
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private lateinit var infantReg : InfantRegCache

    private val dataset =
        ChildRegistrationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(motherBenId)
            val deliveryOutcome = childRegRepo.getDeliveryOutcomeRepoFromMotherBenId(motherBenId = motherBenId)
            infantReg = childRegRepo.getInfantRegFromMotherBenId(motherBenId = motherBenId, babyIndex = babyIndex)!!
            dataset.setUpPage(
                motherBen = ben,
                deliveryOutcomeCache = deliveryOutcome,
                infantRegCache = infantReg,
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
                    val motherBen = benRepo.getBenFromId(motherBenId)!!
                    val childBen = dataset.mapAsBeneficiary(motherBen,preferenceDao.getLoggedInUser()!!, preferenceDao.getLocationRecord()!!)
                    benRepo.substituteBenIdForDraft(childBen)
                    benRepo.persistRecord(childBen)
                    infantReg.childBenId = childBen.beneficiaryId
                    infantRegRepo.update(infantReg)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving child registration data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}
