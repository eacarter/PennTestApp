package com.northwindinteractive.penntestapp

import com.northwindinteractive.penntestapp.data.ble.BeaconReading
import com.northwindinteractive.penntestapp.data.ble.fake.FakeBleSource
import com.northwindinteractive.penntestapp.data.geo.fake.FakeGeofenceSource
import com.northwindinteractive.penntestapp.domain.VenueState
import com.northwindinteractive.penntestapp.domain.VenueStateMachine
import com.northwindinteractive.penntestapp.domain.model.BeaconIdentity
import com.northwindinteractive.penntestapp.domain.model.Venue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PennAppUnitTest {

    val fakeGeofenceSource = FakeGeofenceSource()
    val fakeBleSource = FakeBleSource()
    val venue = testVenue()


    private fun testVenue() = Venue(
        id = "venue_1",
        name = "Coffee Shop Downtown",
        latitude = 42.3314,
        longitude = -83.0458,
        radius = 50.0f,
        beaconIdentity = BeaconIdentity(name = "AABBCCDDEEFF0011AABB", instance = "112233445566")
    )

    @Test
    fun `bluetooth unavailable while inside venue logs without crashing or changing state`() = runTest {
        val stateMachine = VenueStateMachine(
            geofenceSource = fakeGeofenceSource,
            bleSource = fakeBleSource,
            scope = backgroundScope
        )

        stateMachine.onGeofenceEnter(venue)
        runCurrent()
        fakeBleSource.emitReading(venue.id, BeaconReading.bleUnavailable)
        runCurrent()
        assertTrue(stateMachine.state.value is VenueState.Inside)
    }


    @Test
    fun `new reading before timeout resets the timer instead of firing`() = runTest {

        val stateMachine = VenueStateMachine(
            geofenceSource = fakeGeofenceSource,
            bleSource = fakeBleSource,
            scope = backgroundScope
        )

        stateMachine.onGeofenceEnter(venue)
        runCurrent()
        fakeBleSource.emitReading(venue.id, BeaconReading.Detected(venue.id, -70, 0))
        runCurrent()

        advanceTimeBy(9_000.milliseconds)
        fakeBleSource.emitReading(venue.id, BeaconReading.Detected(venue.id, -70, 0)) // resets it
        runCurrent()

        advanceTimeBy(9_000.milliseconds)
        runCurrent()

        assertTrue(stateMachine.state.value is VenueState.InRange)
    }

    @Test
    fun `already inside venue on start results in Inside state`() = runTest {
        fakeGeofenceSource.initialContainmentResult = listOf(venue)

        val stateMachine = VenueStateMachine(
            geofenceSource = fakeGeofenceSource,
            bleSource = fakeBleSource,
            scope = backgroundScope
        )

        stateMachine.start(listOf(venue))

        val state = stateMachine.state.value
        assertTrue(state is VenueState.Inside)
        assertEquals(venue.id, (state as VenueState.Inside).venue.id)
    }

    @Test
    fun `smooths rssi and falls back to Inside after beacon lost timeout`() = runTest {
        val stateMachine = VenueStateMachine(
            geofenceSource = fakeGeofenceSource,
            bleSource = fakeBleSource,
            scope = backgroundScope
        )

        stateMachine.onGeofenceEnter(venue)
        runCurrent()

        fakeBleSource.emitReading(venue.id, BeaconReading.Detected(venue.id, rssi = -70, timestampMs = 0))
        fakeBleSource.emitReading(venue.id, BeaconReading.Detected(venue.id, rssi = -80, timestampMs = 0))
        runCurrent()

        val inRangeState = stateMachine.state.value
        assertTrue(inRangeState is VenueState.InRange)
        assertEquals(-75, (inRangeState as VenueState.InRange).smoothedRssi)

        advanceTimeBy(10_000.milliseconds)
        runCurrent()

        val afterTimeout = stateMachine.state.value
        assertTrue(afterTimeout is VenueState.Inside)
        assertEquals(venue.id, (afterTimeout as VenueState.Inside).venue.id)
    }

    @Test
    fun `exit stops scanning and further readings are ignored`() = runTest {
        val stateMachine = VenueStateMachine(
            geofenceSource = fakeGeofenceSource,
            bleSource = fakeBleSource,
            scope = backgroundScope
        )

        stateMachine.onGeofenceEnter(venue)
        runCurrent()

        fakeBleSource.emitReading(venue.id, BeaconReading.Detected(venue.id, rssi = -65, timestampMs = 0))
        runCurrent()
        assertTrue(stateMachine.state.value is VenueState.InRange)

        val exited = stateMachine.onGeofenceExit(venue.id)
        assertTrue(exited)
        assertEquals(VenueState.Outside, stateMachine.state.value)

        fakeBleSource.emitReading(venue.id, BeaconReading.Detected(venue.id, rssi = -50, timestampMs = 0))
        runCurrent()

        assertEquals(VenueState.Outside, stateMachine.state.value)
    }
}