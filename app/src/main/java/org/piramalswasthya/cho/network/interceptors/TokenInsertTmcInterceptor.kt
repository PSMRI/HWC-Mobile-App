package org.piramalswasthya.cho.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TokenInsertTmcInterceptor : Interceptor{

    companion object {

        fun setCredentials(token: String, jwt: String) {
            AuthTokenManager.setTmcCredentials(token, jwt)
        }

        fun setToken(iToken: String) {
            val credentials = AuthTokenManager.getTmcCredentials()
            AuthTokenManager.setTmcCredentials(iToken, credentials.jwt)
        }

        fun getToken(): String {
            return AuthTokenManager.getTmcCredentials().token
        }

        fun setJwt(iJWT: String) {
            val credentials = AuthTokenManager.getTmcCredentials()
            AuthTokenManager.setTmcCredentials(credentials.token, iJWT)
        }

        fun getJwt(): String {
            return AuthTokenManager.getTmcCredentials().jwt
        }

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            val tmcCredentials = AuthTokenManager.getTmcCredentials()
            request = request
                .newBuilder()
                .addHeader("Authorization", tmcCredentials.token)
                .addHeader("Jwttoken" , tmcCredentials.jwt)
                .build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}
