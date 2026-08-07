package org.piramalswasthya.cho.network.interceptors


import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TokenInsertAbhaInterceptor : Interceptor {
    companion object {
        fun setToken(iToken: String?) {
            AuthTokenManager.setAbhaToken(iToken)
        }

        fun getToken(): String {
            return AuthTokenManager.getAbhaToken()
        }

        fun setXToken(xToken: String?) {
            AuthTokenManager.setAbhaXToken(xToken)
        }

        fun getXToken(): String {
            return AuthTokenManager.getAbhaXToken()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            request = request
                .newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer ${AuthTokenManager.getAbhaToken()}"
                )
                .build()
        }
        val url = request.url.toString()
        if (url.contains("getCard") || url.contains("getPngCard") || url.contains("abha-card")) {
            val xToken = AuthTokenManager.getAbhaXToken()
            if (xToken.isNotEmpty()) {
                request = request
                    .newBuilder()
                    .addHeader(
                        "x-token",
                        "Bearer $xToken"
                    )
                    .build()
            } else {
                Timber.w("x-token is empty, not adding x-token header")
            }
        }

        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}
