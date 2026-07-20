package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.google.gson.Gson
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.model.SnomedDiagnosis
import org.piramalswasthya.cho.network.AmritApiService
import okhttp3.RequestBody
import javax.inject.Inject

class SnomedDiagnosisRepo @Inject constructor(
    private val db: InAppDb,
    private val apiService: AmritApiService,
    private val userRepo: UserRepo
) {
    private val dao get() = db.snomedDiagnosisDao
    val diagnoses: LiveData<List<SnomedDiagnosis>> = dao.getAll()

    suspend fun pullMaster(attempt: Int = 0): Boolean {
        return try {
            val response = apiService.getMasterSnomedCTRecordList(RequestBody.create(null, ByteArray(0)))
            val responseBody = response.body()?.string() ?: return false
            val json = JSONObject(responseBody)
            when (json.optInt("statusCode", response.code())) {
            200 -> {
                val records = json.getJSONObject("data").getJSONArray("sctMaster")
                    .let { Gson().fromJson(it.toString(), Array<SnomedDiagnosis>::class.java).toList() }
                db.withTransaction {
                    dao.deleteAll()
                    dao.insertAll(records)
                }
                true
            }
            5002 -> if (attempt < 1) {
                userRepo.getLoggedInUser()?.let { userRepo.refreshTokenTmc(it.userName, it.password) }
                pullMaster(attempt + 1)
            } else false
            else -> false
            }
        } catch (_: Exception) {
            false
        }
    }
}
