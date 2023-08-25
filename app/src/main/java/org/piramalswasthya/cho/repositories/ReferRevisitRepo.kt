package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.ReferRevisitDao
import org.piramalswasthya.cho.model.ReferRevisitModel
import timber.log.Timber
import javax.inject.Inject



class ReferRevisitRepo @Inject constructor(
    private val referRevisitDao: ReferRevisitDao
) {

    suspend fun saveReferInfoToCache(referRevisitModel: ReferRevisitModel) {
        try{
            withContext(Dispatchers.IO){
                referRevisitDao.insertReferRevisitDetails(referRevisitModel)

            }
        } catch (e: Exception){
            Timber.d("Error in saving refer and revisit information $e")
        }
    }

    fun getReferInfoById(referId:String): LiveData<ReferRevisitModel> {
        return referRevisitDao.getReferDetailsById(referId)
    }

}