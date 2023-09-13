package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictList
import org.piramalswasthya.cho.network.DistrictResponse
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.StateList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import javax.inject.Inject

class DistrictMasterRepo @Inject constructor(private val districtMasterDao: DistrictMasterDao, private val apiService: AmritApiService, private val userRepo: UserRepo) {

    suspend fun districtMasterService(stateId : Int): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.getDistricts(stateId)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = Gson().fromJson(responseBody, DistrictResponse::class.java)
                    val result = DistrictList(data.data)
                    NetworkResult.Success(result)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    districtMasterService(stateId)
                },
            )
        }

    }

    suspend fun insertDistrict(districtMaster: DistrictMaster) {
        districtMasterDao.insertDistrict(districtMaster)
    }

    suspend fun getDistrictByDistrictId(districtId: Int): DistrictMaster {
        return districtMasterDao.getDistrictById(districtId)
    }
    suspend fun getDistrictsByStateId(stateId: Int): List<District> {
        return districtMasterDao.getDistricts(stateId).map { it -> District(it.districtID,it.govtLGDDistrictID, it.districtName) }
    }

}