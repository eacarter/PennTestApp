package com.northwindinteractive.penntestapp.domain

import android.util.Log
import com.northwindinteractive.penntestapp.data.ble.BeaconReading
import com.northwindinteractive.penntestapp.data.ble.BleSource
import com.northwindinteractive.penntestapp.data.geo.GeofenceSource
import com.northwindinteractive.penntestapp.domain.model.Venue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class VenueStateMachine @Inject constructor(
    private val geofenceSource: GeofenceSource,
    private val bleSource: BleSource,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow<VenueState>(VenueState.Outside)
    var state: StateFlow<VenueState> = _state

    private val _log = MutableStateFlow<List<LogEntry>>(emptyList())
    var log: StateFlow<List<LogEntry>> = _log

    private var scanJob: Job? = null
    private var lostTimeoutJob: Job? = null
    private val rssiWindow = mutableListOf<Int>()

    suspend fun start(venues: List<Venue>){
        geofenceSource.registerGeofence(venues)

        val alreadyInside = geofenceSource.checkInitialContainment(venues)
        if(alreadyInside.isNotEmpty()){
            onGeofenceEnter(alreadyInside.first())
        }
    }

    fun onGeofenceEnter(venue : Venue){
        addLog("Entered")
        _state.value = VenueState.Inside(venue)
        rssiWindow.clear()
        startScanning(venue)
    }

    fun onGeofenceExit(venueId: String): Boolean{
        val current = _state.value
        val currentVenueId = when (current) {
            is VenueState.Inside -> current.venue.id
            is VenueState.InRange -> current.venue.id
            VenueState.Outside -> null
        }

        if (currentVenueId != venueId) {
            addLog("Ignored EXIT for $venueId (not currently inside that venue)")
            return false
        }

        addLog("Exited venue $venueId")
        stopScanning()
        _state.value = VenueState.Outside
        return true
    }


    private fun startScanning(venue: Venue){
        scanJob?.cancel()
        scanJob = scope.launch(Dispatchers.Default) {
            bleSource.scan(venue).collect { read ->
                when(read){
                    is BeaconReading.Detected -> handleReading(venue, read)
                    is BeaconReading.bleUnavailable -> addLog("Ble Unavailable")
                }
            }
        }
    }

    private fun stopScanning(){
        scanJob?.cancel()
        scanJob = null
        lostTimeoutJob?.cancel()
        lostTimeoutJob = null
        rssiWindow.clear()
    }

    private fun handleReading(venue: Venue, read : BeaconReading.Detected){
        rssiWindow.add(read.rssi)
        if(rssiWindow.size > 5){
            rssiWindow.removeAt(0)
        }
        val smoothed = rssiWindow.sum() / rssiWindow.size
        val proximity = proximityFor(smoothed)

        addLog("beacon detected")
        _state.value = VenueState.InRange(venue, proximity, smoothed)
        resetLostTimeout(venue)
    }


    private fun resetLostTimeout(venue: Venue) {
        lostTimeoutJob?.cancel()
        lostTimeoutJob = scope.launch {
            delay(10000.milliseconds)
            addLog("Beacon lost for ${venue.name}")
            rssiWindow.clear()
            _state.value = VenueState.Inside(venue)
        }
    }

    private fun proximityFor(rssi: Int): Proximity = when {
        rssi >= -60 -> Proximity.IMMEDIATE
        rssi >= -75 -> Proximity.NEAR
        rssi >= -90 -> Proximity.FAR
        else -> Proximity.UNKNOWN
    }

    private fun addLog(message: String) {
        _log.value += LogEntry(System.currentTimeMillis(), message)
    }
}