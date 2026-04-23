package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import org.piramalswasthya.cho.network.VillageIdList
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.net.SocketTimeoutException

object AmritSyncRepositoryHelper {
    private const val STATUS_OK = 200
    private const val STATUS_TOKEN_EXPIRED = 5002
    private const val STATUS_NO_RECORDS = 5000
    private const val MAX_PULL_ATTEMPTS = 3
    private const val INITIAL_BACKOFF_MS = 500L

    fun convertStringToIntList(villageIds: String): List<Int> {
        if (villageIds.trim().isEmpty()) return emptyList()
        return villageIds.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    suspend fun <T, N> pushUnsynced(
        unsyncedList: List<T>,
        mapToPayload: suspend (T) -> N?,
        post: suspend (List<N>) -> Response<ResponseBody>,
        markSynced: suspend (T) -> Unit,
        logLabel: String
    ): Boolean {
        if (unsyncedList.isEmpty()) return true

        val payload = mutableListOf<N>()
        val sentItems = mutableListOf<T>()
        unsyncedList.forEach { item ->
            val model = mapToPayload(item) ?: return@forEach
            payload.add(model)
            sentItems.add(item)
        }

        if (payload.isEmpty()) return false

        return try {
            val response = post(payload)
            val statusCode = response.body()?.string()?.let(::JSONObject)?.optInt("statusCode")
            if (response.isSuccessful && statusCode == STATUS_OK) {
                sentItems.forEach { markSynced(it) }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing $logLabel")
            false
        }
    }

    suspend fun pullWithRetry(
        villageIds: String,
        lastSyncDate: String,
        fetch: suspend (VillageIdList) -> Response<ResponseBody>,
        refreshToken: suspend () -> Boolean,
        onDataArray: suspend (JSONArray) -> Unit,
        logLabel: String
    ): Boolean {
        var backoffMs = INITIAL_BACKOFF_MS

        repeat(MAX_PULL_ATTEMPTS) { attempt ->
            try {
                val villageList = VillageIdList(convertStringToIntList(villageIds), lastSyncDate)
                val response = fetch(villageList)
                if (!response.isSuccessful) return false

                val responseBody = response.body()?.string() ?: return false
                val jsonObj = JSONObject(responseBody)
                if (jsonObj.isNull("statusCode")) {
                    throw IllegalStateException("Amrit server not responding properly")
                }

                when (jsonObj.getInt("statusCode")) {
                    STATUS_OK -> {
                        val dataArray = jsonObj.optJSONArray("data")
                        if (dataArray != null) {
                            onDataArray(dataArray)
                            return true
                        }
                    }
                    STATUS_TOKEN_EXPIRED -> {
                        val tokenRefreshed = refreshToken()
                        if (!tokenRefreshed) {
                            Timber.w("Token refresh failed while pulling $logLabel")
                            return false
                        }
                        if (attempt < MAX_PULL_ATTEMPTS - 1) {
                            delay(backoffMs)
                            backoffMs *= 2
                            return@repeat
                        }
                        Timber.w("Max retry attempts reached while pulling $logLabel")
                        return false
                    }
                    STATUS_NO_RECORDS -> return true
                }
            } catch (e: SocketTimeoutException) {
                if (attempt < MAX_PULL_ATTEMPTS - 1) {
                    delay(backoffMs)
                    backoffMs *= 2
                    return@repeat
                }
                Timber.e(e, "Socket timeout while pulling $logLabel after retries")
                return false
            } catch (e: Exception) {
                Timber.e(e, "Error pulling $logLabel")
                return false
            }
            return false
        }

        return false
    }

    suspend fun <N> upsertByBeneficiaryRegId(
        dataArray: JSONArray,
        parseNetwork: (JSONObject) -> N,
        beneficiaryRegId: (N) -> Long?,
        resolvePatientId: suspend (Long) -> String?,
        isExisting: suspend (String, N) -> Boolean,
        insertNew: suspend (String, N) -> Unit
    ) {
        for (i in 0 until dataArray.length()) {
            val networkObj = parseNetwork(dataArray.getJSONObject(i))
            val benRegId = beneficiaryRegId(networkObj) ?: continue
            val patientId = resolvePatientId(benRegId) ?: continue
            if (!isExisting(patientId, networkObj)) {
                insertNew(patientId, networkObj)
            }
        }
    }
}
