package com.northwindinteractive.penntestapp.domain.model

data class Venue(
    val id: String,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val radius: Float,
    val beaconIdentity: BeaconIdentity
)
