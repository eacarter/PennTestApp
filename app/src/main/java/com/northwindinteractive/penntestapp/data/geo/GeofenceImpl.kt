package com.northwindinteractive.penntestapp.data.geo

import com.northwindinteractive.penntestapp.domain.model.Venue

interface GeofenceImpl {
    suspend fun registerGeofence(venues: List<Venue>)
    suspend fun unRegisterGeofence()
    suspend fun checkInitialContainment(venues: List<Venue>): List<Venue>
}