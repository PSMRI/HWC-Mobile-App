package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.HealthCenterDao
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject



class DoctorMasterDataMaleRepo @Inject constructor(
    private val healthCenterDao: HealthCenterDao,
    private val amritApiService: AmritApiService

){  private var visitCategoryID: Int = 6
    private var providerServiceMapID: Int = 13
    var gender : String = "Male"
    private var vanID : Int = 61
    private var facilityID : Int = 15
    var apiKey : String = "f5e3e002-8ef8-44cd-9064-45fbc8cad6d5"

    suspend fun getDoctorMasterMaleData() {
        try {
            val response = amritApiService.getDoctorMasterData(
                visitCategoryID, providerServiceMapID,
                gender,facilityID,vanID, apiKey
            )

            if (response.code() == 200) {
                val responseString = response.body()?.string()
                val responseJson = responseString?.let { JSONObject(it) }
                val jsonObject = responseJson?.getJSONObject("data")!!

                val higherHealthCenterListString = jsonObject.getJSONArray("higherHealthCare")
                val higherHealthCenterList = MasterDataListConverter.toHealthCenterList(higherHealthCenterListString.toString())
                saveHigherHealthCenterToCache(higherHealthCenterList)
            }
        } catch (e: Exception) {
            Log.i("Error in Fetching Doctor Master Data", "$e")
        }
    }

    private suspend fun saveHigherHealthCenterToCache(higherHealthCenter:List<HigherHealthCenter>){
            try{
                higherHealthCenter.forEach { healthCenter:HigherHealthCenter ->
                    withContext(Dispatchers.IO){
                        healthCenterDao.insertHealthCenter(healthCenter)
                    }
                }
            } catch (e: Exception){
                Timber.d("Error in saving Higher health center data $e")
            }
        }

    fun getHigherHealthCenter(): LiveData<List<HigherHealthCenter>> {
                return healthCenterDao.getHealthCenter()
            }
        }
