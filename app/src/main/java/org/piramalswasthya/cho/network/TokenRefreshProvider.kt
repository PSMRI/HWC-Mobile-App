package org.piramalswasthya.cho.network

/**
 * Synchronous token refresh provider used by [AuthRefreshInterceptor].
 * Set via [TokenRefreshHolder] after DI is ready to avoid circular dependency
 * (interceptor -> OkHttpClient -> AmritApiService -> UserRepo).
 */
interface TokenRefreshProvider {
    /**
     * Refreshes the JWT/token by calling the login API with stored credentials.
     * @return true if refresh succeeded and token was updated, false otherwise.
     */
    fun refresh(): Boolean
}

/**
 * Holder for the [TokenRefreshProvider] so that [AuthRefreshInterceptor] can
 * obtain it without being injected (which would create a circular dependency).
 */
object TokenRefreshHolder {
    @Volatile
    var provider: TokenRefreshProvider? = null
}
