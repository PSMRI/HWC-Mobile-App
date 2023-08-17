package org.piramalswasthya.cho.repositories

import android.util.Log
import android.util.LogPrinter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.ChiefComplaintMasterDao
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.database.room.dao.SubCatVisitDao
import org.piramalswasthya.cho.model.AlcoholDropdown
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.FamilyMemberDropdown
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.TobaccoDropdown
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.lang.Exception
import java.security.PrivateKey
import javax.inject.Inject

class MaleMasterDataRepository @Inject constructor(
    private val amritApiService: AmritApiService,
    private val chiefComplaintMasterDao: ChiefComplaintMasterDao,
    private val subCatVisitDao: SubCatVisitDao,
    private val historyDao: HistoryDao
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

                val illnessString = jsonObject.getJSONArray("illnessTypes")
                val illnessList = MasterDataListConverter.toIllnessList(illnessString.toString())
                saveIllnessDropdownToCache(illnessList)

                val alcoholString = jsonObject.getJSONArray("alcoholUseStatus")
                val alcoholList = MasterDataListConverter.toAlcoholList(alcoholString.toString())
                saveAlcoholDropdownToCache(alcoholList)

                val allergyString = jsonObject.getJSONArray("AllergicReactionTypes")
                val allergyList = MasterDataListConverter.toAllergyList(allergyString.toString())
                saveAllergyDropdownToCache(allergyList)

                val familyString = jsonObject.getJSONArray("familyMemberTypes")
                val familyList = MasterDataListConverter.toFamilyMemberList(familyString.toString())
                saveFamilyDropdownToCache(familyList)

                val surgeryString = jsonObject.getJSONArray("surgeryTypes")
                val surgeryList = MasterDataListConverter.toSurgeryList(surgeryString.toString())
                saveSurgeryDropdownToCache(surgeryList)

                val tobaccoString = jsonObject.getJSONArray("tobaccoUseStatus")
                val tobaccoList = MasterDataListConverter.toTobaccoList(tobaccoString.toString())
                saveTobaccoDropdownToCache(tobaccoList)
            }
        } catch (e: Exception){
            Log.i("Error in Fetching getMasterDataForNurse()","$e")
        }
    }

    private suspend fun saveSurgeryDropdownToCache(surgeryDropdown:List<SurgeryDropdown>){

        try{
            surgeryDropdown.forEach { surgery: SurgeryDropdown ->
                withContext(Dispatchers.IO){
                    historyDao.insertSurgeryDropdown(surgery)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving Surgery history $e")
        }
    }
    private suspend fun saveFamilyDropdownToCache(familyMemberDropdown:List<FamilyMemberDropdown>){

        try{
            familyMemberDropdown.forEach { family: FamilyMemberDropdown ->
                withContext(Dispatchers.IO){
                    historyDao.insertFamilyMemberDropdown(family)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving Family member history $e")
        }
    }
    private suspend fun saveAllergyDropdownToCache(allergicReactionDropdown:List<AllergicReactionDropdown>){

        try{
            allergicReactionDropdown.forEach { allergy: AllergicReactionDropdown ->
                withContext(Dispatchers.IO){
                    historyDao.insertAllergicReactionDropdown(allergy)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving Allergy history $e")
        }
    }
    private suspend fun saveAlcoholDropdownToCache(alcoholDropdown:List<AlcoholDropdown>){

        try{
            alcoholDropdown.forEach { alcohol: AlcoholDropdown ->
                withContext(Dispatchers.IO){
                    historyDao.insertAlcoholDropdown(alcohol)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving Alcohol history $e")
        }
    }
    private suspend fun saveTobaccoDropdownToCache(tobaccoDropdown:List<TobaccoDropdown>){

        try{
            tobaccoDropdown.forEach { tobacco: TobaccoDropdown ->
                withContext(Dispatchers.IO){
                    historyDao.insertTobaccoDropdown(tobacco)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving Tobacco history $e")
        }
    }

    private suspend fun saveIllnessDropdownToCache(illnessDropdown:List<IllnessDropdown>){

        try{
            illnessDropdown.forEach { illness: IllnessDropdown ->
                withContext(Dispatchers.IO){
                    historyDao.insertIllnessDropdown(illness)
                }
            }
        } catch (e: Exception){
            Timber.d("Error in saving Illness history $e")
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
    fun getAllIllnessDropdown(): LiveData<List<IllnessDropdown>> {
        return historyDao.getAllIllnessDropdown()
    }
    fun getAllTobaccoDropdown(): LiveData<List<TobaccoDropdown>> {
        return historyDao.getAllTobaccoDropdown()
    }
    fun getAllAlcoholDropdown(): LiveData<List<AlcoholDropdown>> {
        return historyDao.getAllAlcoholDropdown()
    }
    fun getAllAllergyDropdown(): LiveData<List<AllergicReactionDropdown>> {
        return historyDao.getAllAllergicReactionDropdown()
    }
    fun getAllFamilyMemberDropdown(): LiveData<List<FamilyMemberDropdown>> {
        return historyDao.getAllFamilyMemberDropdown()
    }
    fun getAllSurgeryDropdown(): LiveData<List<SurgeryDropdown>> {
        return historyDao.getAllSurgeryDropdown()
    }

    fun getChiefMasterComplaint():LiveData<List<ChiefComplaintMaster>>{
          return chiefComplaintMasterDao.getAllChiefCompMaster()
    }
    fun getAllSubCatVisit(): LiveData<List<SubVisitCategory>> {
        return subCatVisitDao.getAllSubCatVisit()
    }
    suspend fun getIllnessByNameMap():Map<Int,String>{
        return historyDao.getIllnessMasterMap().associate {
            it.illnessID to it.illnessType
        }
    }
    suspend fun getSurgeryByNameMap():Map<Int,String>{
        return historyDao.getSurgeryMasterMap().associate {
            it.surgeryID to it.surgeryType
        }
    }

    suspend fun getChiefByNameMap(): Map<Int, String>{
        return chiefComplaintMasterDao.getChiefCompMasterMap().associate {
            it.chiefComplaintID to it.chiefComplaint}
    }
}