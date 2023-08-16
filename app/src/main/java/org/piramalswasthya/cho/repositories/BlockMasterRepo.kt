package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.BlockList
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.DistrictBlockResponse
import org.piramalswasthya.cho.network.DistrictList
import org.piramalswasthya.cho.network.DistrictResponse
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import javax.inject.Inject

class BlockMasterRepo @Inject constructor(private val blockMasterDao: BlockMasterDao, private val apiService: AmritApiService, private val userRepo: UserRepo ) {

    suspend fun blockMasterService(districtId : Int): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.getDistrictBlocks(districtId)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = Gson().fromJson(responseBody, DistrictBlockResponse::class.java)
                    val result = BlockList(data.data)
                    NetworkResult.Success(result)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    blockMasterService(districtId)
                },
            )
        }

//        return withContext(Dispatchers.IO) {
//            try {
//                val response = apiService.getDistrictBlocks(districtId)
//                val responseBody = response.body()?.string()
//                when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
//                    200 -> {
//                        Log.i("user success", "")
////                        val data = responseBody.let { JSONObject(it) }
//                        val result = Gson().fromJson(responseBody, DistrictBlockResponse::class.java)
//                        NetworkResult.Success(result.data.associate { it -> it.blockID to it.blockName })
//                    }
//                    5002 -> {
//                        Log.i("user fetching", "")
//                        val user = userRepo.getLoggedInUser()!!
//                        Log.i("user fetched", user.userName + user.password)
//                        userRepo.refreshTokenTmc(user.userName, user.password)
//                        blockMasterService(districtId)
//                    }
//                    else -> {
//                        Log.i("user exception", "")
//                        NetworkResult.Error(0, responseBody.toString())
//                    }
//                }
//            } catch (e: IOException) {
//                NetworkResult.Error(-1, "Unable to connect to Internet!")
//            } catch (e: JSONException) {
//                NetworkResult.Error(-2, "Invalid response! Please try again!")
//            } catch (e: SocketTimeoutException) {
//                NetworkResult.Error(-3, "Request Timed out! Please try again!")
//            } catch (e: Exception) {
//                NetworkResult.Error(-4, e.message ?: "Unknown Error")
//            }
//        }

//        val response  = apiService.getDistrictBlocks(districtId)
//        return response.data

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

    suspend fun getBlocksByDistrictId(districtId: Int): List<DistrictBlock> {
        return blockMasterDao.getBlocks(districtId).map { it -> DistrictBlock(it.blockID, it.blockName, mapOf()) }
    }

}
