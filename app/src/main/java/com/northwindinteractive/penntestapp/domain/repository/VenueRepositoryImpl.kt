package com.northwindinteractive.penntestapp.domain.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.northwindinteractive.penntestapp.data.repository.VenueRepository
import com.northwindinteractive.penntestapp.domain.model.Venue
import dagger.hilt.android.qualifiers.ApplicationContext

import javax.inject.Inject

class VenueRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) : VenueRepository {

    val tag = "VenueRepo"

    private val venues: List<Venue> by lazy{loadVenues()}

    override fun getAllVenues(): List<Venue> = venues

    override fun getVenueById(id: String): Venue? = venues.find { it.id == id }

    private fun loadVenues(): List<Venue>{
        return try {
            val json = context.assets.open("venues.json")
                .bufferedReader()
                .use{it.readText()}
            Gson().fromJson(json, Array<Venue>::class.java).toList()
        } catch (e: Exception){
            Log.d(tag, "failed to get venues: $e")
            emptyList()
        }
    }
}