package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import javax.inject.Inject

class LanguageRepo @Inject constructor(
    private val languageDao: LanguageDao,
    private val apiService:AmritApiService

){

     suspend fun languageService(): List<Language> {
        val response  = apiService.getLanguagesList()
        val statusCode = response.code()
        if (statusCode == 200) {
            val responseString = response.body()?.string()
            val responseJson = responseString?.let { JSONObject(it) }
            val data = responseJson?.getJSONArray("data")
            return MasterDataListConverter.toLanguageList(data.toString())
        }else{
            throw Exception("Failed to get data!")
        }
    }
    suspend fun saveResponseToCacheLang() {
        languageService().forEach { language: Language ->
            withContext(Dispatchers.IO) {
                languageDao.insertAllLanguages(language)
            }
            Timber.tag("itemLang").d(language.toString())
        }

    }

    suspend fun getCachedResponseLang(): List<Language> {
               return languageDao.getAllLanguages()
    }
}


