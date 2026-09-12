package com.northwindinteractive.penntestapp.data.geo

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.northwindinteractive.penntestapp.domain.model.Venue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeofenceSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofenceClient: GeofencingClient,
    private val locationClient: FusedLocationProviderClient,
    private val geofencePendingIntent: PendingIntent
): GeofenceImpl {
    override suspend fun registerGeofence(venues: List<Venue>) {
        val geofence = venues.map { venue ->
            Geofence.Builder()
                .setRequestId(venue.id)
                .setCircularRegion(venue.latitude, venue.longitude, venue.radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofence)
            .build()

        if (checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        suspendCancellableCoroutine { continuation ->
            geofenceClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener { client -> continuation.resume(client) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
    }

    override suspend fun unRegisterGeofence() {
        suspendCancellableCoroutine { continuation ->
            geofenceClient.removeGeofences(geofencePendingIntent)
                .addOnSuccessListener { client -> continuation.resume(client) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
    }

    override suspend fun checkInitialContainment(venues: List<Venue>): List<Venue> {
        val location = try {
            getBestEffortLocation()
        } catch (e: SecurityException) {
            null
        }

        Log.d("GeofenceSource", "checkInitialContainment resolved location: $location")

        if (location == null) {
            return emptyList()
        }

        val insideVenues = mutableListOf<Venue>()
        for (venue in venues) {
            val distanceResult = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                venue.latitude,
                venue.longitude,
                distanceResult
            )
            if (distanceResult[0] <= venue.radius) {
                insideVenues.add(venue)
            }
        }
        return insideVenues
    }

    private suspend fun getBestEffortLocation(): Location? {
        if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Unit
        }

        val last = suspendCancellableCoroutine<Location?> { continuation ->
            locationClient.lastLocation
                .addOnSuccessListener { loc -> continuation.resume(loc) }
                .addOnFailureListener { continuation.resume(null) }
        }
        if (last != null) {
            Log.d("GeofenceSource", "Using lastLocation: $last")
            return last
        }

        Log.d("GeofenceSource", "lastLocation was null, trying getCurrentLocation")

        val cancellationTokenSource = CancellationTokenSource()
        return suspendCancellableCoroutine { continuation ->
            locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { loc -> continuation.resume(loc) }
                .addOnFailureListener { continuation.resume(null) }
            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
        }
    }
}