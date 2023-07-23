package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.CreateHIDResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.StateList
import org.piramalswasthya.cho.network.StateResponseData
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

    suspend fun getStateList(request : LocationRequest): NetworkResult<Map<Int, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStates(request)
                val responseBody = response.body()?.string()
                when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> {
                        Log.i("user success", "")
                        val data = responseBody.let { JSONObject(it).getString("data") }
                        val result = Gson().fromJson(data, StateList::class.java)
                        NetworkResult.Success(result.stateMaster.associate { it -> it.stateID to it.stateName })
                    }
                    5002 -> {
                        Log.i("user fetching", "")
                        val user = userRepo.getLoggedInUser()!!
                        Log.i("user fetched", user.userName + user.password)
                        userRepo.refreshTokenTmc(user.userName, user.password)
                        getStateList(request)
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
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
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

    suspend fun getAllStatesAsMap(): Map<Int, String> {
        return stateMasterDao.getAllStates().associate { it -> it.stateID to it.stateName }
    }

    suspend fun getCachedResponseLang(): List<StateMaster> {
        return stateMasterDao.getAllStates()
    }
}