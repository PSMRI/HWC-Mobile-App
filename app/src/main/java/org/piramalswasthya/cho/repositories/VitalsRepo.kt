package org.piramalswasthya.cho.repositories

import android.util.Log
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

//    suspend fun getVitalsDetailsByBenRegId(beneficiaryRegID: Long) : PatientVitalsModel?{
//        return vitalsDao.getPatientVitalsByBenRegId(beneficiaryRegID,)
//    }

    suspend fun getVitalsDetailsByPatientID(patientID: String) : PatientVitalsModel {
        return withContext(Dispatchers.IO) {
            vitalsDao.getPatientVitalsByPatientID(patientID)
        }
    }
     fun getVitalsDetailsByPatientIDAndBenVisitNoForFollowUp(patientID: String) : PatientVitalsModel? {
        try {
            return vitalsDao.getPatientVitalsByPatientIDAndBenVisitNoForFollowUp(patientID)

        }catch (e:Exception){
            Log.d("err","ee")
        }
        return null
    }
    suspend fun getPatientVitalsByPatientIDAndBenVisitNo(patientID: String, benVisitNo: Int) : PatientVitalsModel?{
        return vitalsDao.getPatientVitalsByPatientIDAndBenVisitNo(patientID, benVisitNo)
    }

//    suspend fun updateBenFlowId(benFlowId: Long, beneficiaryRegID: Long, benVisitNo: Int) {
//        vitalsDao.updateBenFlowId(benFlowId, beneficiaryRegID, benVisitNo)
//    }

//    suspend fun updateBenIdBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String) {
//        vitalsDao.updateBenIdBenRegId(beneficiaryID, beneficiaryRegID, patientID)
//    }

}
