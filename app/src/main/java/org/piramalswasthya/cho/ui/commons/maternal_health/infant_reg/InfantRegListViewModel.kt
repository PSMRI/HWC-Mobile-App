package org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.InfantRegDomain
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import javax.inject.Inject

@HiltViewModel
class InfantRegListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val infantRegRepo: InfantRegRepo,
    private val patientRepo: PatientRepo
) : ViewModel() {

    val patientID = InfantRegListFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID

    private val _infantList = MutableStateFlow<List<InfantRegDomain>>(emptyList())
    val infantList: StateFlow<List<InfantRegDomain>>
        get() = _infantList

    init {
        viewModelScope.launch {
            loadInfantList()
        }
    }

    private suspend fun loadInfantList() {
        try {
            val deliveryOutcome = deliveryOutcomeRepo.getDeliveryOutcome(patientID)
            val patient = patientRepo.getPatient(patientID)
            val motherName = patient.parentName ?: "Unknown"

            if (deliveryOutcome != null) {
                val numLiveBirths = deliveryOutcome.liveBirth ?: 0
                val list = mutableListOf<InfantRegDomain>()

                for (i in 0 until numLiveBirths) {
                    val savedReg = infantRegRepo.getInfantReg(patientID, i)
                    list.add(
                        InfantRegDomain(
                            motherPatientID = patientID,
                            motherName = motherName,
                            babyIndex = i,
                            deliveryOutcome = deliveryOutcome,
                            savedIr = savedReg
                        )
                    )
                }

                _infantList.value = list
            }
        } catch (e: Exception) {
            _infantList.value = emptyList()
        }
    }
}
