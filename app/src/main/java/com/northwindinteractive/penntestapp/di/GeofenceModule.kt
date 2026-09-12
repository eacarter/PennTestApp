package com.northwindinteractive.penntestapp.di

import android.R.attr.action
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.northwindinteractive.penntestapp.data.geo.GeofenceImpl
import com.northwindinteractive.penntestapp.data.geo.GeofenceSource
import com.northwindinteractive.penntestapp.receiver.GeofenceBroadcastReceiver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeofenceModule {

    /**
     * creates an instance of the Geofence client
     */
    @Provides @Singleton
    fun providesGeofenceClient(@ApplicationContext context: Context): GeofencingClient =
        LocationServices.getGeofencingClient(context)

    /**
     * Creates an instance to get device location
     */
    @Provides @Singleton
    fun providesLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Creates an instance of pending intent broadcast for updating
     */
    @Provides @Singleton
    fun providesGeofencePendingIntent(@ApplicationContext context: Context): PendingIntent{
        return PendingIntent.getBroadcast(
            context,
            1001,
            Intent(context, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class GeofenceBindsModule{
    @Binds @Singleton
    abstract fun bindGeofenceImpl(
        impl: GeofenceSource
    ): GeofenceImpl
}