package org.piramalswasthya.cho.ui.login_activity.login_settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import javax.inject.Inject


@HiltViewModel
class LoginSettingsViewModel@Inject constructor(
    private val pref: PreferenceDao,
    private val stateMasterRepo: StateMasterRepo,
    private val districtMasterRepo: DistrictMasterRepo,
    private val blockMasterRepo: BlockMasterRepo,
    private val villageMasterRepo: VillageMasterRepo,
    private val apiService: AmritApiService
) : ViewModel(){

//    @Inject
//    lateinit var stateMasterRepo: StateMasterRepo
//
//    @Inject
//    lateinit var districtMasterRepo: DistrictMasterRepo
//
//    @Inject
//    lateinit var blockMasterRepo: BlockMasterRepo
//
//    @Inject
//    lateinit var villageMasterRepo: VillageMasterRepo
//
//    @Inject
//    lateinit var apiService: AmritApiService

//    val KARNATAKA_ID = 18

//    private var stateList: List<StateMaster> = mutableListOf()
//
//    private var districtList: List<District> = mutableListOf()
//
//    private var blockList: List<DistrictBlock> = mutableListOf()
//
//    private var villageList: List<Village> = mutableListOf()
//
//    init{
////        val request = LocationRequest(vanID = 153, spPSMID = "64")
//        viewModelScope.launch {
//            addStatesToDb()
//        }
//    }
//
//    fun addStatesToDb(){
//        CoroutineScope(Dispatchers.Main).launch{
////            stateList = stateMasterRepo.getCachedResponseLang()
////            if(stateList.isEmpty()){
//                stateList = stateMasterRepo.stateMasterService()
//                for(state in stateList){
//                    stateMasterRepo.insertStateMaster(state)
//                }
////            }
//        }
//    }
//
//     fun addDistrictsToDb(stateId : Int){
//        CoroutineScope(Dispatchers.Main).launch{
////            var districtList: List<DistrictMaster> = districtMasterRepo.getDistrictsByStateId(stateId)
////            if(districtList.isEmpty()){
//                districtList = districtMasterRepo.districtMasterService(stateId)
//                for(district in districtList){
//                    Log.i("District id is", district.districtID.toString())
//                    districtMasterRepo.insertDistrict(DistrictMaster(districtID = district.districtID, stateID = stateId, districtName = district.districtName))
//                }
////            }
//        }
//    }
//
//    suspend fun addBlocksToDb(districtId : Int): List<DistrictBlock> {
//        CoroutineScope(Dispatchers.Main).launch{
////            var blockList: List<BlockMaster> = blockMasterRepo.blockMasterService(districtId)
////            if(blockList.isEmpty()){
//                blockList = blockMasterRepo.blockMasterService(districtId)
//                for(block in blockList){
//                    Log.i("Block id is", block.blockID.toString())
//                    blockMasterRepo.insertBlock(BlockMaster(blockID = block.blockID, districtID = districtId, blockName = block.blockName))
//                }
//                return blockList;
////            }
//        }
//    }
//
//    fun addVillagesToDb(blockId : Int){
////        CoroutineScope(Dispatchers.Main).launch{
////            var villageList: List<VillageMaster> = villageMasterRepo.villageMasterService(blockId)
////            if(villageList.isEmpty()){
//                villageList = villageMasterRepo.villageMasterService(blockId)
//                for(village in villageList){
//                    Log.i("Village id is", village.districtBranchID.toString())
//                    villageMasterRepo.insertVillage(VillageMaster(districtBranchID = village.districtBranchID, blockID = blockId, villageName = village.villageName))
//                }
////            }
////        }
//    }

}
