package com.northwindinteractive.penntestapp.data.ble.fake

import com.northwindinteractive.penntestapp.domain.model.Venue
import com.northwindinteractive.penntestapp.data.ble.BeaconReading
import com.northwindinteractive.penntestapp.data.ble.BleImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion

class FakeBleSource: BleImpl {
    private val flows = mutableMapOf<String, MutableSharedFlow<BeaconReading>>()
    var stopScanCalledFor: MutableList<String> = mutableListOf()
        private set

    override fun scan(venue: Venue): Flow<BeaconReading> {
        val flow = flows.getOrPut(venue.id) { MutableSharedFlow(replay = 0, extraBufferCapacity = 10) }
        return flow.onCompletion { stopScanCalledFor.add(venue.id) }
    }

    suspend fun emitReading(venueId: String, reading: BeaconReading) {
        flows[venueId]?.emit(reading)
    }
}