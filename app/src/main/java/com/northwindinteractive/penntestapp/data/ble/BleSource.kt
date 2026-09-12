package com.northwindinteractive.penntestapp.data.ble

import android.Manifest
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat.checkSelfPermission
import com.northwindinteractive.penntestapp.domain.model.Venue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class BleSource @Inject constructor(
    @ApplicationContext private val context : Context,
    private val bluetoothLeScanner: BluetoothLeScanner?
): BleImpl{
    override fun scan(venue: Venue): Flow<BeaconReading> = flow {

        val resultChannel = Channel<BeaconReading>(Channel.UNLIMITED)

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val rssi = result.rssi
                val matched = checkIfMatchesVenue(result, venue)
                if (matched) {
                    resultChannel.trySend(
                        BeaconReading.Detected(
                            venueId = venue.id,
                            rssi = rssi,
                            timestampMs = System.currentTimeMillis()
                        )
                    )
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BleScanner", "Scan failed with code $errorCode")
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        val filterList = mutableListOf<ScanFilter>()

        bluetoothLeScanner?.startScan(filterList, settings, scanCallback)

        if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Unit
        }

        try {
            for (reading in resultChannel) {
                emit(reading)
            }
        } finally {
           bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    private fun checkIfMatchesVenue(result: ScanResult, venue: Venue): Boolean { return true
    }

}