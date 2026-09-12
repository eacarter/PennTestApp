package com.northwindinteractive.penntestapp

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.northwindinteractive.penntestapp.data.repository.VenueRepository
import com.northwindinteractive.penntestapp.domain.VenueStateMachine
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PennTestApplication : Application() {

    @Inject
    lateinit var venueStateMachine: VenueStateMachine

    @Inject
    lateinit var venueRepository: VenueRepository

    @Inject
    lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        if (hasRequiredPermissions()) {
            appScope.launch {
                venueStateMachine.start(venueRepository.getAllVenues())
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return fineLocation && backgroundLocation
    }
}