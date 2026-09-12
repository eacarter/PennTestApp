package com.northwindinteractive.penntestapp.ui.composables

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.northwindinteractive.penntestapp.domain.LogEntry
import com.northwindinteractive.penntestapp.domain.VenueState
import com.northwindinteractive.penntestapp.viewmodel.VenueViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PermissionFlow(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(firstStep()) }
    var deniedPermissions by remember { mutableStateOf(setOf<String>()) }

    val fineLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) deniedPermissions = deniedPermissions + Manifest.permission.ACCESS_FINE_LOCATION
        currentStep = nextStep(currentStep)
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) deniedPermissions = deniedPermissions + Manifest.permission.ACCESS_BACKGROUND_LOCATION
        currentStep = nextStep(currentStep)
    }

    val bluetoothScanLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) deniedPermissions = deniedPermissions + Manifest.permission.BLUETOOTH_SCAN
        currentStep = nextStep(currentStep)
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) deniedPermissions = deniedPermissions + Manifest.permission.POST_NOTIFICATIONS
        currentStep = nextStep(currentStep)
    }

    LaunchedEffect(currentStep) {
        when (currentStep) {
            PermissionStep.FINE_LOCATION ->
                fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

            PermissionStep.BACKGROUND_LOCATION ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    currentStep = nextStep(currentStep)
                }

            PermissionStep.BLUETOOTH_SCAN ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bluetoothScanLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
                } else {
                    currentStep = nextStep(currentStep)
                }

            PermissionStep.NOTIFICATIONS ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    currentStep = nextStep(currentStep)
                }

            PermissionStep.DONE -> onComplete()
        }
    }

    if (deniedPermissions.isNotEmpty()) {
        Text("Some permissions were denied: ${deniedPermissions.joinToString()}. The app can't fully track venue visits without them.")
    }
}

@Composable
fun VenueCheckInScreen(viewModel: VenueViewModel = hiltViewModel()) {
    var permissionsHandled by remember { mutableStateOf(false) }

    if (!permissionsHandled) {
        PermissionFlow(onComplete = {
            permissionsHandled = true
            viewModel.onPermissionHandled()
        })
    } else {
        val state by viewModel.state.collectAsStateWithLifecycle()
        val log by viewModel.log.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            VenueStateDisplay(state)
            HorizontalDivider()
            EventLogList(log)
        }
    }
}

@Composable
fun VenueStateDisplay(state: VenueState){
    Log.d("VenueStateDisplay", "Recomposing with state=$state")

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(
            text = "Current State",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        when(state){
            is VenueState.InRange -> {
                Text("Inside: ${state.venue.name}")
                Text("Proximity: ${state.proximity}")
                Text("Smoothing Rssi: ${state.smoothedRssi}")
            }
            is VenueState.Inside -> {
                Text("We're Inside ${state.venue.name}")
                Text("No Beacon detected yet")
            }
            is VenueState.Outside -> {
                Text("We're Outside")
            }
        }
    }
}

@Composable
fun EventLogList(log: List<LogEntry>){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Event Log",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (log.isEmpty()) {
            Text("No events yet")
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(log.reversed()) { entry ->
                    Text(
                        text = "${formatTimestamp(entry.timestampMs)} — ${entry.message}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestampMs: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestampMs))
}

private fun firstStep() = PermissionStep.FINE_LOCATION

private fun nextStep(step: PermissionStep): PermissionStep = when (step) {
    PermissionStep.FINE_LOCATION -> PermissionStep.BACKGROUND_LOCATION
    PermissionStep.BACKGROUND_LOCATION -> PermissionStep.BLUETOOTH_SCAN
    PermissionStep.BLUETOOTH_SCAN -> PermissionStep.NOTIFICATIONS
    PermissionStep.NOTIFICATIONS -> PermissionStep.DONE
    PermissionStep.DONE -> PermissionStep.DONE
}

enum class PermissionStep {
    FINE_LOCATION,
    BACKGROUND_LOCATION,
    BLUETOOTH_SCAN,
    NOTIFICATIONS,
    DONE
}