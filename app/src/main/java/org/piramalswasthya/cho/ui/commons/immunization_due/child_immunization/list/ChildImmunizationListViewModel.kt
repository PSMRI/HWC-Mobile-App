//package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
//import org.piramalswasthya.sakhi.helpers.getTodayMillis
//import org.piramalswasthya.sakhi.model.ImmunizationCategory
//import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
//import org.piramalswasthya.sakhi.model.Vaccine
//import org.piramalswasthya.sakhi.model.VaccineDomain
//import org.piramalswasthya.sakhi.model.VaccineState
//import timber.log.Timber
//import java.util.Calendar
//import javax.inject.Inject
//
//@HiltViewModel
//class ChildImmunizationListViewModel @Inject constructor(
//    vaccineDao: ImmunizationDao
//
//) : ViewModel() {
//    private val pastRecords = vaccineDao.getBenWithImmunizationRecords(
//        minDob = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//            add(Calendar.YEAR, -16)
//        }.timeInMillis,
//        maxDob = System.currentTimeMillis(),
//    )
//    private lateinit var vaccinesList: List<Vaccine>
//
//    val benWithVaccineDetails = pastRecords.map { vaccineIdList ->
//        vaccineIdList.map { cache ->
//            val ageMillis = System.currentTimeMillis() - cache.ben.dob
//            ImmunizationDetailsDomain(ben = cache.ben.asBasicDomainModel(),
//                vaccineStateList = vaccinesList.filter {
//                    it.minAllowedAgeInMillis < ageMillis
//                }.map { vaccine ->
//                    VaccineDomain(
//                        vaccine.id,
//                        vaccine.name,
//                        vaccine.childCategory,
//                        if (cache.givenVaccines.any { it.vaccineId == vaccine.id }) VaccineState.DONE
//                        else if (ageMillis < (vaccine.maxAllowedAgeInMillis)) {
//                            vaccine.dependantVaccineId?.let { dep ->
//
//                                val isDepThere =
//                                    cache.givenVaccines.any { it.vaccineId == vaccine.dependantVaccineId }
//                                if (isDepThere)
//                                    VaccineState.PENDING
//                                else VaccineState.MISSED
//                            }
//                                ?: run { VaccineState.PENDING }
//                        } else VaccineState.MISSED
//
//                    )
//
//                })
//        }
//    }
//
//    private val clickedBenId = MutableStateFlow(0L)
//
//    val bottomSheetContent = clickedBenId.combine(benWithVaccineDetails) { a, b ->
//        b.firstOrNull { it.ben.benId == a }
//
//    }
//
//    init {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                vaccinesList = vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD)
//            }
//        }
//    }
//
//    fun updateBottomSheetData(benId: Long) {
//        viewModelScope.launch {
//            clickedBenId.emit(benId)
//        }
//    }
//
//
//    private fun navigateForClicked(benId: Long, vaccineId: Int) {
//        Timber.d("Hello Me! clicked for $benId, vaccineId : $vaccineId")
//    }
//
//    fun getSelectedBenId(): Long {
//        return clickedBenId.value
//    }
//
//}