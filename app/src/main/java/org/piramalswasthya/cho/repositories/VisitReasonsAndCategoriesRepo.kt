package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.LocationEntityListConverter
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.VisitCategory
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
}