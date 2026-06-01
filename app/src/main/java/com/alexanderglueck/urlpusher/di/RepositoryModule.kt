package com.alexanderglueck.urlpusher.di

import com.alexanderglueck.urlpusher.data.repository.DefaultAuthRepository
import com.alexanderglueck.urlpusher.data.repository.DefaultDevicesRepository
import com.alexanderglueck.urlpusher.data.repository.DefaultUrlsRepository
import com.alexanderglueck.urlpusher.domain.repository.AuthRepository
import com.alexanderglueck.urlpusher.domain.repository.DevicesRepository
import com.alexanderglueck.urlpusher.domain.repository.UrlsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDevicesRepository(impl: DefaultDevicesRepository): DevicesRepository

    @Binds
    @Singleton
    abstract fun bindUrlsRepository(impl: DefaultUrlsRepository): UrlsRepository
}
