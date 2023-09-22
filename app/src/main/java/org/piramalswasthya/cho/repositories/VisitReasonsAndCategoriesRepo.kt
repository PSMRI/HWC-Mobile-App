package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.LocationEntityListConverter
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VisitReason
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import javax.inject.Inject

class VisitReasonsAndCategoriesRepo @Inject constructor(
    private val visitReasonsAndCategoriesDao: VisitReasonsAndCategoriesDao,
    private val apiService: AmritApiService

){

    //VISIT REASONS
    private suspend fun visitReasonsService(): List<VisitReason> {
        val response  = apiService.getVisitReasonAndCategories()
        val statusCode = response.code()
        if (statusCode == 200) {
            val responseString = response.body()?.string()
            val responseJson = responseString?.let { JSONObject(it) }
            val data = responseJson?.getJSONObject("data")
            val visitReasonList = data?.getJSONArray("visitReasons")
            return MasterDataListConverter.toVisitReasonsList(visitReasonList.toString())
        } else{
            throw Exception("Failed to get data!")
        }
    }
    suspend fun saveVisitReasonResponseToCache() {
        visitReasonsService().forEach { visitReason: VisitReason ->
            withContext(Dispatchers.IO) {
                visitReasonsAndCategoriesDao.insertVisitReason(visitReason)
            }
            Timber.tag("ReasonForVisit").d(visitReason.toString())
        }

    }

    suspend fun getVisitReasonCachedResponse(): List<VisitReason> {
        return visitReasonsAndCategoriesDao.getVisitReasons()
    }




    //VISIT CATEGORIES
    private suspend fun visitCategoriesService(): List<VisitCategory> {
        val response  = apiService.getVisitReasonAndCategories()
        val statusCode = response.code()
        if (statusCode == 200) {
            val responseString = response.body()?.string()
            val responseJson = responseString?.let { JSONObject(it) }
            val data = responseJson?.getJSONObject("data")
            val visitCategoryList = data?.getJSONArray("visitCategories")
            return MasterDataListConverter.toVisitCategoryList(visitCategoryList.toString())
        }else{
            throw Exception("Failed to get data!")
        }

    }
    suspend fun saveVisitCategoriesResponseToCache() {

        visitCategoriesService().forEach { visitCategory: VisitCategory ->
            withContext(Dispatchers.IO) {
                visitReasonsAndCategoriesDao.insertVisitCategory(visitCategory)
            }
            Timber.tag("visitCategory").d(visitCategory.toString())
        }

    }

    suspend fun getVisitCategoriesCachedResponse(): List<VisitCategory> {
        return visitReasonsAndCategoriesDao.getVisitCategories()
    }

    suspend fun saveVisitDbToCache(visitDB: VisitDB) {
        try{
            withContext(Dispatchers.IO){
                visitReasonsAndCategoriesDao.insertVisitDB(visitDB)
            }
        } catch (e: Exception){
            Timber.d("Error in saving vitals information $e")
        }
    }

//     fun getVisitDbInfo(id:String): LiveData<VisitDB> {
//        return visitReasonsAndCategoriesDao.getVisitDb(id)
//    }
//
//     fun getChiefComplaintDbInfo(id:String): LiveData<ChiefComplaintDB> {
//        return visitReasonsAndCategoriesDao.getChiefComplaintDb(id)
//    }
    suspend fun saveChiefComplaintDbToCache(chiefComplaintDB: ChiefComplaintDB) {
        try{
            withContext(Dispatchers.IO){
                visitReasonsAndCategoriesDao.insertChiefComplaintDb(chiefComplaintDB)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Chief Complaint Db information $e")
        }
    }

    suspend fun getVisitDB(beneficiaryRegID: Long) : VisitDB?{
        return visitReasonsAndCategoriesDao.getVisitDb(beneficiaryRegID)
    }

    suspend fun getChiefComplaintDB(beneficiaryRegID: Long) : List<ChiefComplaintDB>?{
        return visitReasonsAndCategoriesDao.getChiefComplaints(beneficiaryRegID)
    }

    suspend fun updateBenFlowId(benFlowId: Long, beneficiaryRegID: Long){
        visitReasonsAndCategoriesDao.updateVisitDbBenflow(benFlowId, beneficiaryRegID)
        visitReasonsAndCategoriesDao.updateChiefComplaintsBenflow(benFlowId, beneficiaryRegID)
    }

    suspend fun updateBenIdAndBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String){
        visitReasonsAndCategoriesDao.updateBenIdBenRegIdVisitDb(beneficiaryID, beneficiaryRegID, patientID)
        visitReasonsAndCategoriesDao.updateBenIdBenRegIdChiefComplaint(beneficiaryID, beneficiaryRegID, patientID)
    }



}