package org.piramalswasthya.cho.ui.register_patient_activity.other_informations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.CommunityMaster
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.ReligionMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.register_patient_activity.patient_details.PatientDetailsViewModel
import javax.inject.Inject


@HiltViewModel
class OtherInformationsViewModel @Inject constructor(
    private val registrarMasterDataRepo: RegistrarMasterDataRepo,
    private val patientRepo: PatientRepo,
) : ViewModel() {

    enum class NetworkState {
        IDLE,
        LOADING,
        SUCCESS,
        FAILURE
    }

    private val _religion = MutableLiveData(NetworkState.IDLE)
    val religion: LiveData<NetworkState>
        get() = _religion

    private val _community = MutableLiveData(NetworkState.IDLE)
    val community: LiveData<NetworkState>
        get() = _community

    var communityList: List<CommunityMaster> = mutableListOf()
    var religionList: List<ReligionMaster> = mutableListOf()

    var selectedCommunity: CommunityMaster? = null
    var selectedReligion: ReligionMaster? = null

    init {
        viewModelScope.launch {
            fetchCommunity()
            fetchReligion()
        }
    }

    suspend fun fetchCommunity(){
        _community.value = NetworkState.LOADING
        try {
            communityList = registrarMasterDataRepo.getCommunityMasterCachedResponse()
            _community.value = NetworkState.SUCCESS
        }
        catch (_: Exception){
            _community.value = NetworkState.FAILURE
        }
    }

    suspend fun fetchReligion(){
        _religion.value = NetworkState.LOADING
        try {
            religionList = registrarMasterDataRepo.getReligionMasterCachedResponse()
            _religion.value = NetworkState.SUCCESS
        }
        catch (_: Exception){
            _religion.value = NetworkState.FAILURE
        }
    }

    fun insertPatient(patient: Patient){
        viewModelScope.launch {
            patientRepo.insertPatient(patient)
        }
    }


    // TODO: Implement the ViewModel
}