package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.PMSMAFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PmsmaViewModel @Inject constructor(
    state: SavedStateHandle,
    @ApplicationContext context: Context,
    private val pmsmaRepo: PmsmaRepo,
    private val benRepo: BenRepo,
    maternalHealthRepo: MaternalHealthRepo,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        SUCCESS,
        FAIL
    }

    private val benId = PmsmaFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = PmsmaFragmentArgs.fromSavedStateHandle(state).hhId


    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val dataset = PMSMAFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
//    val popUpString = dataset.alertErrorMessageFlow

    private lateinit var ben: BenRegCache
    private lateinit var pwr: PregnantWomanRegistrationCache
    private var pmsma: PMSMACache? = null

    init {
        Timber.d("init called! ")
        viewModelScope.launch {
            ben = benRepo.getBeneficiaryRecord(benId, hhId)!!
            val household = benRepo.getHousehold(hhId)!!
            pmsma = pmsmaRepo.getPmsmaByBenId(benId)
            pwr = maternalHealthRepo.getSavedRegistrationRecord(benId)!!
            val lastAnc = maternalHealthRepo.getLatestAncRecord(benId)
            _benName.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            _recordExists.value = pmsma != null
            dataset.setUpFirstPage(
                household,
                ben,
                pwr,
                lastAnc,
                if (recordExists.value == true) pmsma else null
            )


        }

    }

    fun submitForm() {
        _state.value = State.LOADING
        val user = preferenceDao.getLoggedInUser()!!
        val pmsmaCache = PMSMACache(
            benId = benId, processed = "N",
            createdBy = user.name, updatedBy = user.name,
            syncState = SyncState.UNSYNCED,
            isActive = true,
        )
        dataset.mapValues(pmsmaCache)
        viewModelScope.launch {
            val saved = pmsmaRepo.savePmsmaData(pmsmaCache)
            if (saved)
                _state.value = State.SUCCESS
            else
                _state.value = State.FAIL
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }
}