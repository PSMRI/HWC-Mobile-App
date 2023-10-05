package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.HealthCenterDao
import org.piramalswasthya.cho.database.room.dao.OutreachDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DrugFormMaster
import org.piramalswasthya.cho.model.DrugFrequencyMaster
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.OutreachDropdownList
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class OutreachRepo @Inject constructor(
    private val outreachDao: OutreachDao,
    private val amritApiService: AmritApiService

){
    var apiKey : String = "f5e3e002-8ef8-44cd-9064-45fbc8cad6d5"
   var stateID:Int = 18
    suspend fun getOutreachDropdownListData() {
        try {
            val response = amritApiService.getOutreachDropdownList(stateID)

            if (response.code() == 200) {
                val responseString = response.body()?.string()
                val responseJson = responseString?.let { JSONObject(it) }
                val jsonObject = responseJson?.getJSONArray("data")!!

                val outreachList = MasterDataListConverter.toOutreachList(jsonObject.toString())
                saveOutreachDropdownListToCache(outreachList)
                Log.d("arr","${outreachList}")
            }
        } catch (e: Exception) {
            Log.i("Error in Fetching Outreach List Data", "$e")
        }
    }
    private suspend fun saveOutreachDropdownListToCache(outreachDropdownList: List<OutreachDropdownList>){
        try{
            outreachDropdownList.forEach { outreachDropdown:OutreachDropdownList ->
                withContext(Dispatchers.IO){
                    outreachDao.insertOutreachList(outreachDropdown)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving Outreach data $e")
        }
    }

     fun getAllOutreachDropdownList(): LiveData<List<OutreachDropdownList>>{
        return outreachDao.getOutreachDropdownList()
    }
    suspend fun getONameMap():Map<Int,String>{
        return outreachDao.getOMap().associate {
            it.id to it.outreachType
        }
    }
}
