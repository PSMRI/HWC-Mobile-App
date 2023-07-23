package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.DistrictBlockResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.network.VillageMasterResponse
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import javax.inject.Inject

class VillageMasterRepo @Inject constructor(private val villageMasterDao: VillageMasterDao, private val apiService: AmritApiService, private val userRepo: UserRepo) {

    suspend fun villageMasterService(blockId : Int): NetworkResult<Map<Int, String>> {

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVillages(blockId)
                val responseBody = response.body()?.string()
                when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> {
                        Log.i("user success", "")
//                        val data = responseBody.let { JSONObject(it) }
                        val result = Gson().fromJson(responseBody, VillageMasterResponse::class.java)
                        NetworkResult.Success(result.data.associate { it -> it.districtBranchID to it.villageName })
                    }
                    5002 -> {
                        Log.i("user fetching", "")
                        val user = userRepo.getLoggedInUser()!!
                        Log.i("user fetched", user.userName + user.password)
                        userRepo.refreshTokenTmc(user.userName, user.password)
                        villageMasterService(blockId)
                    }
                    else -> {
                        Log.i("user exception", "")
                        NetworkResult.Error(0, responseBody.toString())
                    }
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }

//        val response  = apiService.getVillages(blockId)
//        return response.data
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

    suspend fun getBlocksByDistrictIdAsMap(blockId: Int): Map<Int, String> {
        return villageMasterDao.getVillages(blockId).associate { it -> it.districtBranchID to it.villageName }
    }

    suspend fun getVillagesByBlockId(blockId: Int): List<VillageMaster> {
        return villageMasterDao.getVillages(blockId)
    }
}