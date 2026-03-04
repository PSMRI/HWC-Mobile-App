package org.piramalswasthya.cho.network.interceptors

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.piramalswasthya.cho.network.TokenRefreshHolder
import timber.log.Timber

/**
 * OkHttp interceptor that detects expired JWT (HTTP 401/403 or API statusCode 5002),
 * refreshes the token via login API, and retries the original request once.
 * Prevents sync/API failures when the app is kept open overnight and the token expires.
 */
class AuthRefreshInterceptor : Interceptor {

    companion object {
        private const val HEADER_NO_AUTH = "No-Auth"
        /** Header added on retry so we only retry once. */
        private const val HEADER_RETRY = "X-Auth-Retry"
        private const val API_STATUS_TOKEN_EXPIRED = 5002
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Do not add token or try refresh for login/refresh requests
        if (request.header(HEADER_NO_AUTH) != null) {
            return chain.proceed(request)
        }

        // If this is already a retry after refresh, don't try again
        if (request.header(HEADER_RETRY) != null) {
            return chain.proceed(request)
        }

        val response = chain.proceed(request)

        // HTTP-level auth failure: refresh and retry once
        if (response.code == 401 || response.code == 403) {
            if (performRefreshAndRetry(chain, request)) {
                response.close()
                return retryRequest(chain, request)
            }
            return response
        }

        // Success response might still have API-level token expiry (statusCode 5002)
        if (response.code == 200 && response.body != null) {
            val contentType = response.body!!.contentType()
            val bodyString = response.body!!.string()
            response.close()

            val statusCode = try {
                JSONObject(bodyString).optInt("statusCode", -1)
            } catch (_: Exception) {
                -1
            }

            if (statusCode == API_STATUS_TOKEN_EXPIRED) {
                Timber.w("AuthRefreshInterceptor: API returned statusCode 5002 (token expired)")
                if (performRefreshAndRetry(chain, request)) {
                    return retryRequest(chain, request)
                }
                // Return the original error response so caller can handle
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Token expired")
                    .body(bodyString.toResponseBody(contentType))
                    .build()
            }

            // Rebuild response with consumed body so caller can read it
            return response.newBuilder()
                .body(bodyString.toResponseBody(contentType))
                .build()
        }

        return response
    }

    private fun performRefreshAndRetry(chain: Interceptor.Chain, request: Request): Boolean {
        val provider = TokenRefreshHolder.provider
        if (provider == null) {
            Timber.w("AuthRefreshInterceptor: TokenRefreshProvider not set, cannot refresh")
            return false
        }
        return try {
            val refreshed = provider.refresh()
            if (refreshed) {
                Timber.d("AuthRefreshInterceptor: Token refreshed successfully, retrying request")
            } else {
                Timber.w("AuthRefreshInterceptor: Token refresh failed (no user or API error)")
            }
            refreshed
        } catch (e: Exception) {
            Timber.e(e, "AuthRefreshInterceptor: Error during token refresh")
            false
        }
    }

    private fun retryRequest(chain: Interceptor.Chain, originalRequest: Request): Response {
        val retryRequest = originalRequest.newBuilder()
            .header(HEADER_RETRY, "1")
            .build()
        return chain.proceed(retryRequest)
    }
}
