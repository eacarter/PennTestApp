package com.northwindinteractive.penntestapp.di

import com.northwindinteractive.penntestapp.data.repository.VenueRepository
import com.northwindinteractive.penntestapp.domain.repository.VenueRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VenueModule {

    @Binds
    @Singleton
    abstract fun bindVenueRepository(
        impl: VenueRepositoryImpl
    ): VenueRepository
}