package com.northwindinteractive.penntestapp.domain

import com.northwindinteractive.penntestapp.domain.model.Venue

sealed interface VenueState {
    data object Outside : VenueState
    data class Inside(val venue: Venue) : VenueState
    data class InRange(
        val venue: Venue,
        val proximity: Proximity,
        val smoothedRssi: Int
    ) : VenueState
}

enum class Proximity {
    IMMEDIATE, NEAR, FAR, UNKNOWN
}

data class LogEntry(
    val timestampMs: Long,
    val message: String
)