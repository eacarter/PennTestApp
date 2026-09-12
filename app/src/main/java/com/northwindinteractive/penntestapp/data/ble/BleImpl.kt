package com.northwindinteractive.penntestapp.data.ble

import com.northwindinteractive.penntestapp.domain.model.Venue
import kotlinx.coroutines.flow.Flow

sealed interface BeaconReading {
    data class Detected(val venueId: String, val rssi: Int, val timestampMs: Long) : BeaconReading
    data object bleUnavailable : BeaconReading
}

interface BleImpl {
    fun scan(venue: Venue): Flow<BeaconReading>
}