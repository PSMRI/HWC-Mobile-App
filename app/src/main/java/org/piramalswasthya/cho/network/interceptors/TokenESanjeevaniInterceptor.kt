package org.piramalswasthya.cho.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TokenESanjeevaniInterceptor : Interceptor{

    companion object {
        private var TOKEN: String = ""
        fun setToken(iToken: String) {
            TOKEN = iToken
        }

        fun getToken(): String {
            return TOKEN
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            request = request
                .newBuilder()
                .addHeader("Authorization", "Bearer $TOKEN")
                .build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}