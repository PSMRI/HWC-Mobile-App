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
            val rawResponseBody = response.body()?.string()
            val statusCode = rawResponseBody?.let { body ->
                runCatching { JSONObject(body).optInt("statusCode") }.getOrNull()
            }
            if (!response.isSuccessful) {
                Timber.w(
                    "Failed syncing %s: httpCode=%d, statusCode=%s, body=%s",
                    logLabel,
                    response.code(),
                    statusCode,
                    rawResponseBody
                )
                false
            } else if (statusCode == STATUS_OK) {
                sentItems.forEach { markSynced(it) }
                true
            } else {
                Timber.w(
                    "Failed syncing %s: httpCode=%d, statusCode=%s, body=%s",
                    logLabel,
                    response.code(),
                    statusCode,
                    rawResponseBody
                )
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
                var response = fetch(villageList)
                var attemptedTokenRefreshRetry = false

                while (true) {
                    if (!response.isSuccessful) {
                        val httpCode = response.code()
                        val rawResponseBody = response.errorBody()?.string()
                        Timber.w(
                            "HTTP failure while pulling %s: code=%d, body=%s, attempt=%d/%d",
                            logLabel,
                            httpCode,
                            rawResponseBody,
                            attempt + 1,
                            MAX_PULL_ATTEMPTS
                        )

                        if (httpCode in 400..499) return false

                        if (attempt < MAX_PULL_ATTEMPTS - 1) {
                            delay(backoffMs)
                            backoffMs *= 2
                            return@repeat
                        }
                        Timber.w("Max retry attempts reached for HTTP failure while pulling $logLabel")
                        return false
                    }

                    val responseBody = response.body()?.string()
                    if (responseBody.isNullOrBlank()) {
                        Timber.e("Empty response body while pulling $logLabel, code=${response.code()}")
                        return false
                    }

                    val jsonObj = try {
                        JSONObject(responseBody)
                    } catch (e: Exception) {
                        Timber.e(e, "Invalid JSON while pulling $logLabel, body=$responseBody")
                        return false
                    }

                    if (jsonObj.isNull("statusCode")) {
                        Timber.e(
                            "Missing statusCode while pulling %s, code=%d, body=%s",
                            logLabel,
                            response.code(),
                            responseBody
                        )
                        return false
                    }

                    val statusCode = jsonObj.getInt("statusCode")
                    when (statusCode) {
                        STATUS_OK -> {
                            val dataArray = jsonObj.optJSONArray("data")
                            if (dataArray != null && dataArray.length() > 0) {
                                onDataArray(dataArray)
                            }
                            return true
                        }
                        STATUS_TOKEN_EXPIRED -> {
                            val tokenRefreshed = refreshToken()
                            if (!tokenRefreshed) {
                                Timber.w("Token refresh failed while pulling $logLabel")
                                return false
                            }

                            if (attemptedTokenRefreshRetry) {
                                Timber.w("Token expired again after refresh retry while pulling $logLabel")
                                return false
                            }

                            attemptedTokenRefreshRetry = true
                            response = fetch(villageList)
                        }
                        STATUS_NO_RECORDS -> return true
                        else -> {
                            Timber.w(
                                "Unknown statusCode while pulling %s: statusCode=%d, code=%d, body=%s",
                                logLabel,
                                statusCode,
                                response.code(),
                                responseBody
                            )
                            return false
                        }
                    }
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
