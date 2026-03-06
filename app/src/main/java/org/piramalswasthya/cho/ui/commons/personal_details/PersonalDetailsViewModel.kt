package org.piramalswasthya.cho.ui.commons.personal_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.utils.filterBenList
import javax.inject.Inject

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */

@HiltViewModel
class PersonalDetailsViewModel @Inject constructor(
    private val patientRepo: PatientRepo,
    private val pref: PreferenceDao,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo
) : ViewModel() {
    private val filter = MutableStateFlow("")
    private val listUpdateDebounceMs = 250L

    private val latestVisitComparator =
        compareByDescending<PatientDisplayWithVisitInfo> { it.visitDate?.time ?: 0L }
            .thenByDescending { it.benVisitNo ?: 0 }

    private fun buildPatientListFlow(
        source: Flow<List<PatientDisplayWithVisitInfo>>,
        transform: (List<PatientDisplayWithVisitInfo>) -> List<PatientDisplayWithVisitInfo>
    ): Flow<List<PatientDisplayWithVisitInfo>> {
        return source
            .map(transform)
            .combine(filter) { list, query ->
                filterBenList(list, query)
            }
            .debounce(listUpdateDebounceMs)
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    val patientListForNurse: Flow<List<PatientDisplayWithVisitInfo>> =
        buildPatientListFlow(
            source = patientRepo.getPatientDisplayListForNurse(),
            transform = { list -> list }
        )

    val patientListForDoctor: Flow<List<PatientDisplayWithVisitInfo>> =
        buildPatientListFlow(
            source = patientRepo.getPatientDisplayListForDoctor(),
            transform = { list -> list }
        )

    val patientListForLab: Flow<List<PatientDisplayWithVisitInfo>> =
        buildPatientListFlow(
            source = patientVisitInfoSyncRepo.getPatientDisplayListForLab(),
            transform = { list -> list }
        )

    val patientListForPharmacist: Flow<List<PatientDisplayWithVisitInfo>> =
        buildPatientListFlow(
            source = patientVisitInfoSyncRepo.getPatientListFlowForPharmacist(),
            transform = { list -> list.sortedWith(latestVisitComparator) }
        )

    var count : Int = 0
    private val _abha = MutableLiveData<String?>()
    val abha: LiveData<String?>
        get() = _abha

    private val _benId = MutableLiveData<Long?>()
    val benId: LiveData<Long?>
        get() = _benId

    private val _benRegId = MutableLiveData<Long?>()
    val benRegId: LiveData<Long?>
        get() = _benRegId


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
            _patientObserver.value = NetworkState.SUCCESS
        }
    }

    fun filterText(text: String) {
        filter.value = text
    }

    fun fetchAbha(benId: Long) {
        _abha.value = null
        _benRegId.value = null
        _benId.value = benId
        viewModelScope.launch {
            patientRepo.getBenFromId(benId)?.let {
                _benRegId.value = it.beneficiaryRegID
//                val result = it.beneficiaryRegID?.let { it1 -> patientRepo.getBeneficiaryWithId(it1) }
//                if (result != null) {
//                    _abha.value = result.healthIdNumber
//                    it.healthIdDetails = BenHealthIdDetails(result.healthId, result.healthIdNumber)
//                    patientRepo.updateRecord(it)
//                } else {
//                    _benRegId.value = it.beneficiaryRegID
//                }
            }
        }
    }

    fun resetBenRegId() {
        _benRegId.value = null
    }
    fun rememberUserEsanjeevani(username: String,password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pref.registerEsanjeevaniCred(username,password)
            }
        }
    }
    fun forgetUserEsanjeevani() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pref.deleteEsanjeevaniCreds()
            }
        }
    }
    fun fetchRememberedPassword(): String? =
        pref.getEsanjeevaniPassword()
    fun fetchRememberedUsername(): String? =
        pref.getEsanjeevaniUserName()

}
