package org.piramalswasthya.cho.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class ContentTypeInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
        return chain.proceed(request)
    }
}