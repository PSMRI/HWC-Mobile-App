package org.piramalswasthya.cho.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TokenInsertTmcInterceptor : Interceptor{

    companion object {

        private var TOKEN: String = ""
        fun setToken(iToken: String) {
            TOKEN = iToken
        }

        fun getToken(): String {
            return TOKEN
        }

        private var JWT: String = ""
        fun setJwt(iJWT: String) {
            JWT = iJWT
        }

        fun getJwt(): String {
            return JWT
        }

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            request = request
                .newBuilder()
                .addHeader("Authorization", TOKEN)
                .addHeader("Jwttoken" , JWT)
                .build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}