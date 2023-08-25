package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.model.PatientVitalsModel
import timber.log.Timber
import javax.inject.Inject


class VitalsRepo @Inject constructor(
    private val vitalsDao: VitalsDao
) {

    suspend fun saveVitalsInfoToCache(patientVitalsModel: PatientVitalsModel) {
        try{
            withContext(Dispatchers.IO){
                vitalsDao.insertPatientVitals(patientVitalsModel)
            }
        } catch (e: Exception){
            Timber.d("Error in saving vitals information $e")
        }
    }

    fun getVitalsInfo(vitalsId:String): LiveData<PatientVitalsModel> {
        return vitalsDao.getPatientVitalsById(vitalsId)
    }

}
