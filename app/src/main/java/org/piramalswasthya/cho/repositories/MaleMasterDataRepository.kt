package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.ChiefComplaintMasterDao
import org.piramalswasthya.cho.database.room.dao.SubCatVisitDao
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class MaleMasterDataRepository @Inject constructor(
    private val amritApiService: AmritApiService,
    private val chiefComplaintMasterDao: ChiefComplaintMasterDao,
    private val subCatVisitDao: SubCatVisitDao
) {
    private var visitCategoryID: Int = 6
    private var providerServiceMapID: Int = 13
    var gender : String = "Male"
    var apiKey : String = "f5e3e002-8ef8-44cd-9064-45fbc8cad6d5"


    suspend fun getMasterDataForNurse(){
        try {
            val response = amritApiService.getNurseMasterData(
                visitCategoryID, providerServiceMapID,
                gender, apiKey)

            if (response.code() == 200) {
                val responseString = response.body()?.string()
                val responseJson = responseString?.let { JSONObject(it) }
                val jsonObject = responseJson?.getJSONObject("data")!!

                val subCatListString = jsonObject.getJSONArray("subVisitCategories")
                val subCatList = MasterDataListConverter.toSubCatVisitList(subCatListString.toString())
                saveSubVisitCategoriesToCache(subCatList)

                val chiefComplaintString = jsonObject.getJSONArray("chiefComplaintMaster")
                val chiefComplaintMasterList = MasterDataListConverter.toChiefMasterComplaintList(chiefComplaintString.toString())
                saveChiefComplaintMasterToCache(chiefComplaintMasterList)
            }
        } catch (e: Exception){
            Log.i("Error in Fetching getMasterDataForNurse()","$e")
        }
    }

    private suspend fun saveSubVisitCategoriesToCache(subCatList:List<SubVisitCategory>){
        try{
            subCatList.forEach { subVisitCategory: SubVisitCategory ->
                withContext(Dispatchers.IO){
                    subCatVisitDao.insertSubCat(subVisitCategory)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving subCat visit $e")
        }
    }

    private suspend fun saveChiefComplaintMasterToCache(chiefComplaintMasterList: List<ChiefComplaintMaster>){
        try{
            chiefComplaintMasterList.forEach { chiefComplaintMaster: ChiefComplaintMaster ->
                withContext(Dispatchers.IO){
                    chiefComplaintMasterDao.insertChiefCompMaster(chiefComplaintMaster)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving chief complaint master data")
        }
    }

    fun getAllSubCatVisit(): LiveData<List<SubVisitCategory>> {
         return subCatVisitDao.getAllSubCatVisit()
    }

    fun getChiefMasterComplaint():LiveData<List<ChiefComplaintMaster>>{
          return chiefComplaintMasterDao.getAllChiefCompMaster()
    }

}