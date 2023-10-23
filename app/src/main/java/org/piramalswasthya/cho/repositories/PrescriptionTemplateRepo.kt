package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.PrescriptionTemplateDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.ProcedureDataWithComponent
import timber.log.Timber
import javax.inject.Inject

class PrescriptionTemplateRepo @Inject constructor(
    private val prescriptionTemplateDao: PrescriptionTemplateDao,
) {
    suspend fun savePrescriptionTemplateToCache(prescriptionTemplateDB: PrescriptionTemplateDB) {
        try{
            withContext(Dispatchers.IO){
                prescriptionTemplateDao.insertPrescriptionTemplates(prescriptionTemplateDB)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Template $e")
        }
    }
    fun getProceduresWithComponent(userID: Int): LiveData<List<PrescriptionTemplateDB?>>{
        return prescriptionTemplateDao.getTemplateForUser(userID)
    }
}