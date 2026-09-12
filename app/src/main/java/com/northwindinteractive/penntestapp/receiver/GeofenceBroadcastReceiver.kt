package com.northwindinteractive.penntestapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.northwindinteractive.penntestapp.data.repository.VenueRepository
import com.northwindinteractive.penntestapp.services.VenueScanningService
import com.northwindinteractive.penntestapp.domain.VenueStateMachine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class GeofenceBroadcastReceiver: BroadcastReceiver() {

    val tag = "GeofenceBroadcastReceiver"

    override fun onReceive(p0: Context, p1: Intent) {
        Log.d("GeofenceReceiver", "onReceive fired, action=${p1.action}")
        val geofenceEvent = GeofencingEvent.fromIntent(p1)

        geofenceEvent.let {
            if(it == null){
                Log.d(tag, "Event was null")
                return
            }
            if(it.hasError()){
                Log.d(tag, "Event error: ${GeofenceStatusCodes.getStatusCodeString(geofenceEvent!!.errorCode)}")
                return
            }
        }

        val transitionType = geofenceEvent!!.geofenceTransition
        Log.d(tag, "transitionType=$transitionType")
        val triggerGeofence = geofenceEvent.triggeringGeofences
        Log.d(tag, "triggeringGeofences=${triggerGeofence?.map { it.requestId }}")

        triggerGeofence.let {
            if(it == null){
                Log.d(tag, "No geofence triggered")
                return
            }
            else{
                for(geofence in it){
                    val venueId = geofence.requestId

                   val foregroundServiceIntent = Intent(p0, VenueScanningService::class.java).apply {
                       putExtra(VenueScanningService.EXTRA_VENUE_ID, venueId)
                       action = when(transitionType){
                           Geofence.GEOFENCE_TRANSITION_ENTER -> VenueScanningService.ACTION_ENTER
                           Geofence.GEOFENCE_TRANSITION_EXIT -> VenueScanningService.ACTION_EXIT
                           else -> return
                       }
                   }

                    ContextCompat.startForegroundService(p0, foregroundServiceIntent)
                }
            }
        }

    }

}