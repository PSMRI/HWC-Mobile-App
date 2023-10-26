package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.dao.PrescriptionTemplateDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import timber.log.Timber
import java.lang.IllegalStateException
import javax.inject.Inject

class PrescriptionTemplateRepo @Inject constructor(
    private val prescriptionTemplateDao: PrescriptionTemplateDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao
) {
    suspend fun saveTemplateToServer(prescriptionTemplateDB: List<PrescriptionTemplateDB>): NetworkResult<NetworkResponse> {
            return networkResultInterceptor {
                val response = amritApiService.sendTemplateToServer(prescriptionTemplateDB)
                val responseBody = response.body()?.string()
                refreshTokenInterceptor(
                    responseBody = responseBody,
                    onSuccess = {
                        Log.i("doctor data submitted",  response.body()?.string() ?: "")
//                    val data = responseBody.let { JSONObject(it).getString("data") }
//                    val result = Gson().fromJson(data, NurseDataResponse::class.java)
                        NetworkResult.Success(NetworkResponse())
                    },
                    onTokenExpired = {
                        val user = userRepo.getLoggedInUser()!!
                        userRepo.refreshTokenTmc(user.userName, user.password)
                        saveTemplateToServer(prescriptionTemplateDB)
                    },
                )
            }
    }

    suspend fun getTemplateFromServer(userID: Int): NetworkResult<NetworkResponse> {
        return networkResultInterceptor {
            val response = amritApiService.getTemplateFromServer(userID)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val responseString = responseBody ?: throw IllegalStateException("Response empty!!!")
                    Timber.i("doctor data submitted",  responseString)
                    val json = JSONObject(responseString)
                    val data = json.getJSONArray("data").toString()

                    val list  = Gson().fromJson(data, Array<PrescriptionTemplateDB>::class.java)
                    saveAllPrescriptionTemplateToCache(list)

//
//                    val data = responseBody.let { JSONObject(it).getString("data") }
//                    val result = Gson().fromJson(data, NurseDataResponse::class.java)
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getTemplateFromServer(userID)
                },
            )
        }
    }
    suspend fun savePrescriptionTemplateToCache(prescriptionTemplateDB: PrescriptionTemplateDB) {
        try{
            withContext(Dispatchers.IO){
                prescriptionTemplateDao.insertPrescriptionTemplates(prescriptionTemplateDB)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Template $e")
        }
    }

    suspend fun saveAllPrescriptionTemplateToCache(prescriptionTemplateDB: Array<PrescriptionTemplateDB>) {
        try{
            withContext(Dispatchers.IO){
                prescriptionTemplateDao.insertPrescriptionTemplates(*prescriptionTemplateDB)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Template $e")
        }
    }
    suspend fun getProceduresWithComponent(userId : Int):List<PrescriptionTemplateDB?>{
        return prescriptionTemplateDao.getTemplateForUser(userId)
    }
}