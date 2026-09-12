package com.northwindinteractive.penntestapp.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import com.northwindinteractive.penntestapp.data.ble.BleImpl
import com.northwindinteractive.penntestapp.data.ble.BleSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    /**
     * Creates an instance of the BluetoothManager
     */
    @Provides @Singleton
    fun providesBluetoothManager(@ApplicationContext context: Context): BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    /**
     * Creates an instance of Bluetooth Adapter
     */
    @Provides @Singleton
    fun providesBluetoothAdapter(bluetoothManager: BluetoothManager?): BluetoothAdapter? =
        bluetoothManager?.adapter

    /**
     * Create an instance of the Bluetooth Scanner
     */
    @Provides @Singleton
    fun providesBluetoothLeScanner(bluetoothAdapter: BluetoothAdapter?): BluetoothLeScanner? =
        bluetoothAdapter?.bluetoothLeScanner
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BleModuleBinder{

    @Binds @Singleton
    abstract fun bindBleScanner(
        impl: BleSource
    ): BleImpl
}