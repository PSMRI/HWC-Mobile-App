package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.model.CovidVaccinationStatusHistory
import org.piramalswasthya.cho.model.AssociateAilmentsHistory
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PastSurgeryHistory
import org.piramalswasthya.cho.model.TobaccoAlcoholHistory
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class HistoryRepo @Inject constructor(
    private val historyDao: HistoryDao
) {

  suspend fun saveAssociateAilmentsHistoryToCatche(associateAilmentsHistory: AssociateAilmentsHistory) {
      try{
          withContext(Dispatchers.IO){
              historyDao.insertAssociateAilmentsHistory(associateAilmentsHistory)
          }
      } catch (e: Exception){
          Timber.d("Error in saving Associate Ailments history $e")
      }
  }
    suspend fun saveMedicationHistoryToCatche(medicationHistory: MedicationHistory) {
        try{
            withContext(Dispatchers.IO){
                historyDao.insertMedicationHistory(medicationHistory)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Medication history $e")
        }
    }
  suspend fun saveCovidVaccinationStatusHistoryToCatche(covidVaccinationStatusHistory: CovidVaccinationStatusHistory) {
      try{
              withContext(Dispatchers.IO){
                  historyDao.insertCovidVaccinationStatusHistory(covidVaccinationStatusHistory)
              }
      } catch (e: Exception){
          Timber.d("Error in saving Covid Vaccination Status history $e")
      }
  }
    suspend fun savePastSurgeryHistoryToCatche(pastSurgeryHistory: PastSurgeryHistory) {
        try{
            withContext(Dispatchers.IO){
                historyDao.insertPastSurgeryHistory(pastSurgeryHistory)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Past Surgery history $e")
        }
    }
    suspend fun savePastIllnessHistoryToCatche(pastIllnessHistory: PastIllnessHistory) {
        try{
            withContext(Dispatchers.IO){
                historyDao.insertPastIllnessHistory(pastIllnessHistory)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Past Illness history $e")
        }
    }

    suspend fun saveTobAndAlcHistoryToCatche(tobaccoAlcoholHistory: TobaccoAlcoholHistory) {
        try{
            withContext(Dispatchers.IO){
                historyDao.insertTobAndAlcHistory(tobaccoAlcoholHistory)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Medication history $e")
        }
    }


    fun getCovidVaccinationStatusHistory(medicationId:String): LiveData<MedicationHistory> {
        return historyDao.getMedicationHistoryByMedicationId(medicationId)
    }
    fun getPastIllnessHistory(medicationId:String): LiveData<MedicationHistory> {
        return historyDao.getMedicationHistoryByMedicationId(medicationId)
    }
    fun getPastSurgeryHistory(medicationId:String): LiveData<MedicationHistory> {
        return historyDao.getMedicationHistoryByMedicationId(medicationId)
    }
    fun getMedicationHistory(medicationId:String): LiveData<MedicationHistory> {
        return historyDao.getMedicationHistoryByMedicationId(medicationId)
    }
    fun getTobAndAlcHistory(tobAndAlcId:String): TobaccoAlcoholHistory {
        return historyDao.getTobAndAlcHistory(tobAndAlcId)
    }

    fun getAssociateAilmentsHistory(tobAndAlcId:String): TobaccoAlcoholHistory {
        return historyDao.getTobAndAlcHistory(tobAndAlcId)
    }

}