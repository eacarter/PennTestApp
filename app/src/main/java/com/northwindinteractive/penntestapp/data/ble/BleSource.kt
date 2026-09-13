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

        Log.d("BleSource", "scan() flow started for venue=${venue.id}")

        if (checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            emit(BeaconReading.bleUnavailable)
            return@flow
        }

        if (bluetoothLeScanner == null) {
            emit(BeaconReading.bleUnavailable)
            return@flow
        }

        val resultChannel = Channel<BeaconReading>(Channel.UNLIMITED)

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val rssi = result.rssi
                Log.d("BleSource", "onScanResult: device=${result.device.address}, rssi=${result.rssi}")
                val matched = checkIfMatchesVenue(result, venue)
                Log.d("BleSource", "matched=$matched")
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

        bluetoothLeScanner.startScan(emptyList(), settings, scanCallback)
        Log.d("BleSource", "startScan() called, scanner=$bluetoothLeScanner")

        try {
            for (reading in resultChannel) {
                emit(reading)
            }
        } finally {
            bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    private fun checkIfMatchesVenue(result: ScanResult, venue: Venue): Boolean {
        val serviceData = result.scanRecord?.getServiceData(
            ParcelUuid(UUID.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"))
        ) ?: return false

        if (serviceData.size < 17 || serviceData[0] != 0x00.toByte()) return false

        val namespace = serviceData.copyOfRange(1, 11).toHex()
        val instance = serviceData.copyOfRange(11, 17).toHex()

        return namespace.equals(venue.beaconIdentity.name, ignoreCase = true) &&
                instance.equals(venue.beaconIdentity.instance, ignoreCase = true)
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02X".format(it) }

}