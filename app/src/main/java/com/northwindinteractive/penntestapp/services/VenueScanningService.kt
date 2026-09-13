package com.northwindinteractive.penntestapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.R
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.northwindinteractive.penntestapp.data.repository.VenueRepository
import com.northwindinteractive.penntestapp.domain.VenueStateMachine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VenueScanningService : Service() {
    @Inject
    lateinit var venueStateMachine: VenueStateMachine

    @Inject
    lateinit var venueRepository: VenueRepository


    companion object {
        const val ACTION_ENTER = "action_enter"
        const val ACTION_EXIT = "action_exit"
        const val EXTRA_VENUE_ID = "extra_venue_id"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "venue_scanning_channel"
    }


    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val venueId = intent?.getStringExtra(EXTRA_VENUE_ID)

        when (intent?.action) {
            ACTION_ENTER -> {
                val venue = venueId?.let { venueRepository.getVenueById(venueId) }
                Log.d("VSS", "ENTER branch, resolved venue=$venue")
                if (venue == null) {
                    Log.e("VSS", "Unknown venue id: $venueId")
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForeground(NOTIFICATION_ID, buildNotification(venue.name))
                venueStateMachine.onGeofenceEnter(venue)
            }

            ACTION_EXIT -> {
                Log.d("VSS", "EXIT branch")
                val actuallyExited = venueId?.let { venueStateMachine.onGeofenceExit(it) } ?: false
                if (actuallyExited) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }

            else -> {
                Log.d("VSS", "unknown action")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }


    private fun buildNotification(venueName: String): Notification {
        createNotificationChannelIfNeeded()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Staus: $venueName")
            .setContentText("Scanning beacons near by")
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannelIfNeeded() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Venue Scanning",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}