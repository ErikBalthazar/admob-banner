package com.erikbalthazar.admobbanner.di

import com.erikbalthazar.admobbanner.data.source.ads.AdRequestFactory
import com.erikbalthazar.admobbanner.data.source.ads.AdRequestFactoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dagger Hilt module for providing Ad-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    @Provides
    fun provideAdRequestFactory(): AdRequestFactory = AdRequestFactoryImpl()
}