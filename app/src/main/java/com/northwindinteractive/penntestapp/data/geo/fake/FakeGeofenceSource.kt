package com.northwindinteractive.penntestapp.data.geo.fake

import com.northwindinteractive.penntestapp.domain.model.Venue
import com.northwindinteractive.penntestapp.data.geo.GeofenceImpl

class FakeGeofenceSource : GeofenceImpl {
    var registeredVenue:List<Venue> = emptyList()
        private set
    var wasUnregistered = false
        private set
    var initialContainmentResult: List<Venue> = emptyList()

    override suspend fun registerGeofence(venues: List<Venue>) {
        registeredVenue = venues
    }

    override suspend fun unRegisterGeofence() {
        wasUnregistered = true
    }

    override suspend fun checkInitialContainment(venues: List<Venue>): List<Venue> {
        return initialContainmentResult
    }

}