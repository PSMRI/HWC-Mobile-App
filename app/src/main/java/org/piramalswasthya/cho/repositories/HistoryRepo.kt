package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.AssociateAilmentsHistory
import org.piramalswasthya.cho.model.FamilyMemberDropdown
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.TobaccoAlcoholHistory
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.lang.AssertionError
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

    suspend fun saveTobAndAlcHistoryToCatche(tobaccoAlcoholHistory: TobaccoAlcoholHistory) {
        try{
            withContext(Dispatchers.IO){
                historyDao.insertTobAndAlcHistory(tobaccoAlcoholHistory)
            }
        } catch (e: Exception){
            Timber.d("Error in saving Medication history $e")
        }
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