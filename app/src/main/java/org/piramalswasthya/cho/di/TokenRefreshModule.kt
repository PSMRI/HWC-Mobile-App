package org.piramalswasthya.cho.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.cho.network.TokenRefreshProvider
import org.piramalswasthya.cho.network.TokenRefreshProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenRefreshModule {

    @Binds
    @Singleton
    abstract fun bindTokenRefreshProvider(impl: TokenRefreshProviderImpl): TokenRefreshProvider
}
