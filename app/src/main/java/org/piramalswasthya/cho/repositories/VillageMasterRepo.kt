package org.piramalswasthya.cho.repositories

import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.Village
import timber.log.Timber
import javax.inject.Inject

class VillageMasterRepo @Inject constructor(private val villageMasterDao: VillageMasterDao, private val apiService: AmritApiService) {

    suspend fun villageMasterService(blockId : Int): List<Village> {
        val response  = apiService.getVillages(blockId)
        return response.data
//        val statusCode = response.code()
//        if (statusCode == 200) {
//            Timber.tag("CODE").d(response.code().toString())
//            val responseString = response.body()?.string()
//            val responseJson = responseString?.let { JSONObject(it) }
//            val data = responseJson?.getJSONArray("data")
//            return MasterDataListConverter.toVillageMasterList(data.toString())
//        }
//        else{
//            throw Exception("Failed to get data!")
//        }
    }

    suspend fun insertVillage(villageMaster: VillageMaster) {
        villageMasterDao.insertVillage(villageMaster)
    }

    suspend fun getVillagesByBlockId(blockId: Int): List<VillageMaster> {
        return villageMasterDao.getVillages(blockId)
    }
}