package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BeneficiariesDTO
import org.piramalswasthya.cho.model.OutreachActivityModel
import org.piramalswasthya.cho.model.OutreachActivityNetworkModel
import org.piramalswasthya.cho.model.PatientDoctorFormUpsync
import org.piramalswasthya.cho.network.AbhaApiService
import org.piramalswasthya.cho.network.ActivityResponse
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import javax.inject.Inject

class ActivityRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
)  {

    suspend fun saveNewActivity(activity: OutreachActivityModel): NetworkResult<NetworkResponse> {

        val user = userRepo.getLoggedInUser()!!
        val networkModel = OutreachActivityNetworkModel(user, activity)

        return networkResultInterceptor {
            val response = amritApiService.createNewActivity(networkModel)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    Log.i("activity data submitted",  response.body()?.string() ?: "")
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    saveNewActivity(activity)
                },
            )
        }
    }

    suspend fun getActivityByUser(): NetworkResult<NetworkResponse> {

        val user = userRepo.getLoggedInUser()!!

        return networkResultInterceptor {
            val response = amritApiService.getActivityByUser(user.userId)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    val gson = Gson()
                    val dataListType = object : TypeToken<List<OutreachActivityNetworkModel>>() {}.type
                    val activities : List<OutreachActivityNetworkModel> = gson.fromJson(data, dataListType)
                    NetworkResult.Success(ActivityResponse(activities))
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getActivityByUser()
                },
            )
        }
    }

    suspend fun getActivityById(activityId: Int): NetworkResult<NetworkResponse> {

        val user = userRepo.getLoggedInUser()!!

        return networkResultInterceptor {
            val response = amritApiService.getActivityByUser(user.userId)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    val gson = Gson()
                    val dataListType = object : TypeToken<List<OutreachActivityNetworkModel>>() {}.type
                    val activities : List<OutreachActivityNetworkModel> = gson.fromJson(data, dataListType)
                    NetworkResult.Success(ActivityResponse(activities))
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getActivityById(activityId)
                },
            )
        }
    }

}