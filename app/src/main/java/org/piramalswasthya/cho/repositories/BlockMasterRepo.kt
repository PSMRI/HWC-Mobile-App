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

    }

    suspend fun insertBlock(blockMaster: BlockMaster) {
        blockMasterDao.insertBlock(blockMaster)
    }

    suspend fun getBlocksByDistrictId(districtId: Int): List<DistrictBlock> {
        return blockMasterDao.getBlocks(districtId).map { it -> DistrictBlock(it.blockID,it.govLGDSubDistrictID, it.blockName, mapOf()) }
    }
    suspend fun getBlocksById(blockId: Int): BlockMaster {
        return blockMasterDao.getBlockById(blockId)
    }

}
