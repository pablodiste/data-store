package dev.pablodiste.datastore.sample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pablodiste.datastore.sample.network.KtorManager
import dev.pablodiste.datastore.sample.network.KtorStarWarsService
import javax.inject.Singleton

private const val TIME_OUT = 60_000

@Module
@InstallIn(SingletonComponent::class)
class KtorApiModule {

    @Provides
    @Singleton
    fun provideKtorManager(): KtorManager = KtorManager()

    @Provides
    @Singleton
    fun provideKtorStarWarsApiService(ktorManager: KtorManager): KtorStarWarsService = ktorManager.createService(KtorStarWarsService::class.java)
}

