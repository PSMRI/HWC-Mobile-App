package org.piramalswasthya.cho.repositories

import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.DistrictBlock
import timber.log.Timber
import javax.inject.Inject

class BlockMasterRepo @Inject constructor(private val blockMasterDao: BlockMasterDao, private val apiService: AmritApiService) {

    suspend fun blockMasterService(districtId : Int): List<DistrictBlock> {
        val response  = apiService.getDistrictBlocks(districtId)
        return response.data
//        val statusCode = response.code()
//        if (statusCode == 200) {
//            Timber.tag("CODE").d(response.code().toString())
//            val responseString = response.body()?.string()
//            val responseJson = responseString?.let { JSONObject(it) }
//            val data = responseJson?.getJSONArray("data")
//            return MasterDataListConverter.toBlockMasterList(data.toString())
//        }
//        else{
//            throw Exception("Failed to get data!")
//        }
    }

    suspend fun insertBlock(blockMaster: BlockMaster) {
        blockMasterDao.insertBlock(blockMaster)
    }

    suspend fun getBlocksByDistrictId(districtId: Int): List<BlockMaster> {
        return blockMasterDao.getBlocks(districtId)
    }
}
