package com.northwindinteractive.penntestapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.northwindinteractive.penntestapp.data.repository.VenueRepository
import com.northwindinteractive.penntestapp.domain.VenueStateMachine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class VenueViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val venueStateMachine: VenueStateMachine,
): ViewModel(){
    val state = venueStateMachine.state
    val log = venueStateMachine.log

    fun onPermissionHandled(){
        viewModelScope.launch {
            venueStateMachine.start(venueRepository.getAllVenues())
        }
    }
}