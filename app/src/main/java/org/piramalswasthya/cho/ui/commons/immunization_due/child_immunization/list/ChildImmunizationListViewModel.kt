package org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.ImmunizationDao
import org.piramalswasthya.cho.helpers.getTodayMillis
import org.piramalswasthya.cho.model.ImmunizationCategory
import org.piramalswasthya.cho.model.ImmunizationDetailsDomain
import org.piramalswasthya.cho.model.Vaccine
import org.piramalswasthya.cho.model.VaccineDomain
import org.piramalswasthya.cho.model.VaccineState
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChildImmunizationListViewModel @Inject constructor(
    vaccineDao: ImmunizationDao

) : ViewModel() {
    private val pastRecords = vaccineDao.getBenWithImmunizationRecords(
        minDob = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.YEAR, -16)
        }.timeInMillis,
        maxDob = System.currentTimeMillis(),
    )
    private lateinit var vaccinesList: List<Vaccine>

    val benWithVaccineDetails = pastRecords.map { vaccineIdList ->
        vaccineIdList.map { cache ->
            val ageMillis = System.currentTimeMillis() - cache.ben.dob!!.time
            ImmunizationDetailsDomain(ben = cache.ben,
                vaccineStateList = vaccinesList.filter {
                    it.minAllowedAgeInMillis < ageMillis
                }.map { vaccine ->
                    VaccineDomain(
                        vaccine.vaccineId,
                        vaccine.vaccineName,
                        vaccine.immunizationService,
                        if (cache.givenVaccines.any { it.vaccineId == vaccine.vaccineId }) VaccineState.DONE
                        else if (ageMillis < (vaccine.maxAllowedAgeInMillis)) {
                            vaccine.dependantVaccineId?.let { dep ->

                                val isDepThere =
                                    cache.givenVaccines.any { it.vaccineId == vaccine.dependantVaccineId }
                                if (isDepThere)
                                    VaccineState.PENDING
                                else VaccineState.MISSED
                            }
                                ?: run { VaccineState.PENDING }
                        } else VaccineState.MISSED

                    )

                })
        }
    }

    private val clickedBenId = MutableStateFlow("")

    val bottomSheetContent = clickedBenId.combine(benWithVaccineDetails) { a, b ->
        b.firstOrNull { it.ben.patientID == a }

    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                vaccinesList = vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD)
            }
        }
    }

    fun updateBottomSheetData(benId: String) {
        viewModelScope.launch {
            clickedBenId.emit(benId)
        }
    }


    private fun navigateForClicked(benId: Long, vaccineId: Int) {
        Timber.d("Hello Me! clicked for $benId, vaccineId : $vaccineId")
    }

    fun getSelectedBenId(): String {
        return clickedBenId.value
    }

}