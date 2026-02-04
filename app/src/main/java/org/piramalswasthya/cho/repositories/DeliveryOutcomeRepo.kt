package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.helpers.setToStartOfTheDay
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.DeliveryOutcomePost
import org.piramalswasthya.cho.network.AmritApiService
//import org.piramalswasthya.cho.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeliveryOutcomeRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val deliveryOutcomeDao: DeliveryOutcomeDao
) {

    suspend fun getDeliveryOutcome(patientID: String): DeliveryOutcomeCache? {
        return withContext(Dispatchers.IO) {
            deliveryOutcomeDao.getDeliveryOutcome(patientID)
        }
    }

    suspend fun saveDeliveryOutcome(deliveryOutcomeCache: DeliveryOutcomeCache) {
        withContext(Dispatchers.IO) {
            // Upsert pattern: check for existing record by patientID and reuse its id if found
            val existing = deliveryOutcomeDao.getDeliveryOutcome(deliveryOutcomeCache.patientID)
            
            if (existing != null) {
                // Reuse existing record's id and update
                val updatedCache = deliveryOutcomeCache.copy(id = existing.id)
                val rowsAffected = deliveryOutcomeDao.updateDeliveryOutcome(updatedCache)
                if (rowsAffected == 0) {
                    // Update failed (record not found by id), insert instead
                    deliveryOutcomeDao.saveDeliveryOutcome(updatedCache)
                }
            } else if (deliveryOutcomeCache.id != 0L) {
                // Has id but not found in DB, try update first
                val rowsAffected = deliveryOutcomeDao.updateDeliveryOutcome(deliveryOutcomeCache)
                if (rowsAffected == 0) {
                    // Update failed (id doesn't exist), insert instead
                    deliveryOutcomeDao.saveDeliveryOutcome(deliveryOutcomeCache)
                }
            } else {
                // New record, insert
                deliveryOutcomeDao.saveDeliveryOutcome(deliveryOutcomeCache)
            }
        }
    }

}