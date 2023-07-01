package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import javax.inject.Inject

class StateMasterRepo @Inject constructor(
    private val stateMasterDao: StateMasterDao,
    private val apiService: AmritApiService

){

    private suspend fun stateMasterService(): List<StateMaster> {
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
    suspend fun saveStateMasterResponseToCache() {
        stateMasterService().forEach { stateMaster : StateMaster ->
            withContext(Dispatchers.IO) {
                stateMasterDao.insertStates(stateMaster)
            }
            Timber.tag("itemStateMaster").d(stateMaster.toString())
        }
    }

    suspend fun getCachedResponseLang(): List<StateMaster> {
        return stateMasterDao.getAllStates()
    }
}