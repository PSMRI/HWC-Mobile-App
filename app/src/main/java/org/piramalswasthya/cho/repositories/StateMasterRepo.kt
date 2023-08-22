package org.piramalswasthya.cho.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.CreateHIDResponse
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.StateList
import org.piramalswasthya.cho.network.StateResponseData
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class StateMasterRepo @Inject constructor(
    private val stateMasterDao: StateMasterDao,
    private val apiService: AmritApiService,
    private val userRepo: UserRepo,
){

    suspend fun stateMasterService(): List<StateMaster> {
        val response  = apiService.getStatesMasterList()
        val statusCode = response.code()
        if (statusCode == 200) {
            Timber.tag("CODE").d(response.code().toString())
            val responseString = response.body()?.string()
            val responseJson = responseString?.let { JSONObject(it) }
            val data = responseJson?.getJSONArray("data")
            return MasterDataListConverter.toStatesMasterList(data.toString())
        }
        else{
            throw Exception("Failed to get data!")
        }
    }

    suspend fun getStateList(request : LocationRequest): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.getStates(request)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    val result = Gson().fromJson(data, StateList::class.java)
                    NetworkResult.Success(result)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getStateList(request)
                },
            )
        }

    }

    suspend fun saveStateMasterResponseToCache() {
        stateMasterService().forEach { stateMaster : StateMaster ->
            withContext(Dispatchers.IO) {
                stateMasterDao.insertStates(stateMaster)
            }
            Timber.tag("itemStateMaster").d(stateMaster.toString())
        }
    }

    suspend fun insertStateMaster(stateMaster: StateMaster){
        stateMasterDao.insertStates(stateMaster)
    }

    suspend fun getAllStates(): List<State> {
        return stateMasterDao.getAllStates().map { it -> State(it.stateID, it.stateName) }
    }

    suspend fun getCachedResponseLang(): List<StateMaster> {
        return stateMasterDao.getAllStates()
    }
}