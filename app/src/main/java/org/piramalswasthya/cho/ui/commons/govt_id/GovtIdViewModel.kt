package org.piramalswasthya.cho.ui.commons.govt_id

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentOutreachBinding
import org.piramalswasthya.cho.repositories.GovIdEntityMasterRepo
import org.piramalswasthya.cho.repositories.LanguageRepo
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.UserAuthRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import javax.inject.Inject

@HiltViewModel
class GovtIdViewModel  @Inject constructor(
    private val govIdEntityMasterRepo: GovIdEntityMasterRepo
): ViewModel() {

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        viewModel = ViewModelProvider(this).get(OutreachViewModel::class.java)
//        _binding = FragmentOutreachBinding.inflate(layoutInflater, container, false)
//        val options = FirebaseVisionFaceDetectorOptions.Builder()
//            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
//            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
//            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//            .build()
//
//        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)
//
//        return binding.root
//    }

    init {
        viewModelScope.launch {
//            languageRepo.saveResponseToCacheLang()
//            visitReasonsAndCategoriesRepo.saveVisitReasonResponseToCache()
//            visitReasonsAndCategoriesRepo.saveVisitCategoriesResponseToCache()
//            registrarMasterDataRepo.saveGenderMasterResponseToCache()
//            registrarMasterDataRepo.saveAgeUnitMasterResponseToCache()
//            registrarMasterDataRepo.saveIncomeMasterResponseToCache()
//            registrarMasterDataRepo.saveLiteracyStatusServiceResponseToCache()
//            registrarMasterDataRepo.saveCommunityMasterResponseToCache()
//            registrarMasterDataRepo.saveMaritalStatusServiceResponseToCache()
//            registrarMasterDataRepo.saveGovIdEntityMasterResponseToCache()
//            registrarMasterDataRepo.saveOtherGovIdEntityMasterResponseToCache()
//            registrarMasterDataRepo.saveOccupationMasterResponseToCache()
//            registrarMasterDataRepo.saveQualificationMasterResponseToCache()
//            registrarMasterDataRepo.saveReligionMasterResponseToCache()
//            registrarMasterDataRepo.saveOccupationMasterResponseToCache()
//            registrarMasterDataRepo.saveRelationshipMasterResponseToCache()
//            stateMasterRepo.saveStateMasterResponseToCache()
//            vaccineAndDoseTypeRepo.saveVaccineTypeResponseToCache()
//            vaccineAndDoseTypeRepo.saveDoseTypeResponseToCache()
        }
    }

    private suspend fun fetchGovtIds(){
//        val govtIdMap = govIdEntityMasterRepo.getGovIdtEntityAsMap()
//        if(govtIdMap != null){
//            val govtIdNames = govtIdMap.values.toTypedArray()
//            binding.dropdownGovtIdType.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, govtIdNames))
//            if(govtIdNames.isNotEmpty()) {
//                selectedGovtIdType = GovIdEntityMaster( govtIdMap!!.entries.toList()[0].key, govtIdMap!!.entries.toList()[0].value)
//                binding.dropdownGovtIdType.setText(selectedGovtIdType!!.identityType, false)
//            }
//            binding.dropdownGovtIdType.setOnItemClickListener { parent, _, position, _ ->
//                selectedGovtIdType = GovIdEntityMaster( govtIdMap!!.entries.toList()[position].key, govtIdMap!!.entries.toList()[position].value)
//                binding.dropdownGovtIdType.setText(selectedGovtIdType!!.identityType, false)
//            }
//        }
    }


}