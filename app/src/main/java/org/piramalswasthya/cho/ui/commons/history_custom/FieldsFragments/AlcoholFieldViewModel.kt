//package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import dagger.hilt.android.lifecycle.HiltViewModel
//import org.piramalswasthya.cho.model.AlcoholDropdown
//import org.piramalswasthya.cho.model.IllnessDropdown
//import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
//import timber.log.Timber
//import java.lang.Exception
//import javax.inject.Inject
//
//@HiltViewModel
//class AlcoholFieldViewModel @Inject constructor(
//    private val maleMasterDataRepository: MaleMasterDataRepository
//): ViewModel() {
//    private var _alcoholDropdown: LiveData<List<AlcoholDropdown>>
//
//    val alcoholDropdown: LiveData<List<AlcoholDropdown>>
//        get() = _alcoholDropdown
//
//    init{
//        _alcoholDropdown = MutableLiveData()
//        getAlcoholDropdown()
//    }
//    private fun getAlcoholDropdown(){
//        try{
//            _alcoholDropdown  = maleMasterDataRepository.getAllAlcoholDropdown()
//
//        } catch (e: Exception){
//            Timber.d("Error in getAlcoholList() $e")
//        }
//    }
//}