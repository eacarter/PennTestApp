package com.northwindinteractive.penntestapp.data.repository

import com.northwindinteractive.penntestapp.domain.model.Venue

interface VenueRepository {
    fun getAllVenues(): List<Venue>
    fun getVenueById(id: String): Venue?
}