package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.helpers.filterPwAncList
import org.piramalswasthya.cho.model.AncStatus
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class PwAncVisitsListViewModel @Inject constructor(
    recordsRepo: RecordsRepo, private val maternalHealthRepo: MaternalHealthRepo
) : ViewModel() {
    private val allBenList = recordsRepo.getRegisteredPregnantWomanList()
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterPwAncList(list, filter)
    }

    private val benIdSelected = MutableStateFlow(0L)

    private val _bottomSheetList = benList.combine(benIdSelected) { list, benId ->
        if (benId != 0L)
            list.first { it.ben.benId == benId }.anc
        else
            emptyList()
    }
    val bottomSheetList: Flow<List<AncStatus>>
        get() = _bottomSheetList


    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
//            val _list = mutableListOf<AncStatus>()
//            val regis = maternalHealthRepo.getSavedRegistrationRecord(benId)!!
//            val filledForms = maternalHealthRepo.getAllAncRecords(benId)
//            val millisToday = Calendar.getInstance().setToStartOfTheDay().timeInMillis
//            val list = getAncStatusList(filledForms, regis.lmpDate, benId, millisToday)
//                listOf(1, 2, 3, 4).map {
//                getAncStatus(filledForms, regis.lmpDate, it, benId, millisToday)
//            }
//            Timber.d("list emitted $list")
//            _bottomSheetList.emit(emptyList())
//            _bottomSheetList.emit(list)
            benIdSelected.emit(benId)
        }
    }


}