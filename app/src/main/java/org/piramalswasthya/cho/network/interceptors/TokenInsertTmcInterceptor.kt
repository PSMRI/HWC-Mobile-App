package org.piramalswasthya.cho.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TokenInsertTmcInterceptor : Interceptor{

    companion object {

        fun setToken(iToken: String) {
            AuthTokenManager.setTmcToken(iToken)
        }

        fun getToken(): String {
            return AuthTokenManager.getTmcToken()
        }

        fun setJwt(iJWT: String) {
            AuthTokenManager.setTmcJwt(iJWT)
        }

        fun getJwt(): String {
            return AuthTokenManager.getTmcJwt()
        }

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            request = request
                .newBuilder()
                .addHeader("Authorization", AuthTokenManager.getTmcToken())
                .addHeader("Jwttoken" , AuthTokenManager.getTmcJwt())
                .build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}
