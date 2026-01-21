package org.piramalswasthya.cho.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.InfantRegDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InfantRegPost
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class InfantRegRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val infantRegDao: InfantRegDao
) {

    suspend fun getInfantReg(patientID: String, babyIndex: Int): InfantRegCache? {
        return withContext(Dispatchers.IO) {
            infantRegDao.getInfantReg(patientID, babyIndex)
        }
    }

    suspend fun getInfantRegFromChildPatientID(childPatientID: String): InfantRegCache? {
        return withContext(Dispatchers.IO) {
            infantRegDao.getInfantRegFromChildPatientID(childPatientID)
        }
    }

    suspend fun saveInfantReg(infantRegCache: InfantRegCache) {
        withContext(Dispatchers.IO) {
            infantRegDao.saveInfantReg(infantRegCache)
        }
    }

    suspend fun updateInfantReg(infantRegCache: InfantRegCache) {
        withContext(Dispatchers.IO) {
            infantRegDao.updateInfantReg(infantRegCache)
        }
    }

    suspend fun getNumBabyRegistered(patientID: String): Int {
        return withContext(Dispatchers.IO) {
            infantRegDao.getNumBabiesRegistered(patientID)
        }
    }

    suspend fun getAllUnprocessedInfantReg(): List<InfantRegCache> {
        return withContext(Dispatchers.IO) {
            infantRegDao.getAllUnprocessedInfantReg()
        }
    }

    suspend fun setToInactive(patientIDs: Set<String>) {
        withContext(Dispatchers.IO) {
            val records = infantRegDao.getAllInfantRegs(patientIDs)
            records.forEach {
                it.isActive = false
                if (it.processed != "N") it.processed = "U"
                it.syncState = SyncState.UNSYNCED
                it.updatedDate = System.currentTimeMillis()
                infantRegDao.updateInfantReg(it)
            }
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
            val dateString = dateFormat.format(millis)
            val timeString = timeFormat.format(millis)
            return "${dateString}T${timeString}.000Z"
        }
    }
}
