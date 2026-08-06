package org.piramalswasthya.cho.network.interceptors


import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TokenInsertAbhaInterceptor : Interceptor {
    companion object {
        private var TOKEN: String = ""
        private var XToken: String = ""
        fun setToken(iToken: String?) {
            TOKEN = iToken ?: ""
        }

        fun getToken(): String {
            return TOKEN
        }

        fun setXToken(xToken: String?) {
            XToken = xToken ?: ""
        }

        fun getXToken(): String {
            return XToken
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            request = request
                .newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer $TOKEN"
                )
                .build()
        }
        val url = request.url.toString()
        if (url.contains("getCard") || url.contains("getPngCard") || url.contains("abha-card")) {
            if (XToken.isNotEmpty()) {
                request = request
                    .newBuilder()
                    .addHeader(
                        "x-token",
                        "Bearer $XToken"
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
