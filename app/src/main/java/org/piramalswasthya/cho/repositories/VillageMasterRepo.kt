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
import org.piramalswasthya.cho.network.BlockList
import org.piramalswasthya.cho.network.DistrictBlockResponse
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.network.VillageList
import org.piramalswasthya.cho.network.VillageMasterResponse
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import javax.inject.Inject

class VillageMasterRepo @Inject constructor(private val villageMasterDao: VillageMasterDao, private val apiService: AmritApiService, private val userRepo: UserRepo) {

    suspend fun villageMasterService(blockId : Int): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.getVillages(blockId)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = Gson().fromJson(responseBody, VillageMasterResponse::class.java)
                    val result = VillageList(data.data)
                    NetworkResult.Success(result)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    villageMasterService(blockId)
                },
            )
        }

    }

    suspend fun insertVillage(villageMaster: VillageMaster) {
        villageMasterDao.insertVillage(villageMaster)
    }

    suspend fun getVillagesByBlockId(blockId: Int): List<Village> {
        return villageMasterDao.getVillages(blockId).map { it -> Village(it.districtBranchID, it.govtLGDVillageID, it.villageName, mapOf()) }
    }

}