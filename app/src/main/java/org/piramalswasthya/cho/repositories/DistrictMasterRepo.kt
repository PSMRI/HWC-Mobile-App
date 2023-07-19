package org.piramalswasthya.cho.repositories

import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import timber.log.Timber
import javax.inject.Inject

class DistrictMasterRepo @Inject constructor(private val districtMasterDao: DistrictMasterDao, private val apiService: AmritApiService) {

    suspend fun districtMasterService(stateId : Int): List<District> {
        val response  = apiService.getDistricts(stateId)
        return response.data
//        val statusCode = response.code()
//        if (statusCode == 200) {
//            Timber.tag("CODE").d(response.code().toString())
//            val responseString = response.body()?.string()
//            val responseJson = responseString?.let { JSONObject(it) }
//            val data = responseJson?.getJSONArray("data")
//            return MasterDataListConverter.toDistrictMasterList(data.toString())
//        }
//        else{
//            throw Exception("Failed to get data!")
//        }
    }

    suspend fun insertDistrict(districtMaster: DistrictMaster) {
        districtMasterDao.insertDistrict(districtMaster)
    }

    suspend fun getDistrictsByStateId(stateId: Int): List<DistrictMaster> {
        return districtMasterDao.getDistricts(stateId)
    }
}