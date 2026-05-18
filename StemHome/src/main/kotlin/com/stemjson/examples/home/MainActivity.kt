package com.stemjson.examples.home

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.stemjson.runtime.StemRender
import com.stemjson.runtime.StemRuntime
import com.stemjson.runtime.StemValidationOutcome
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Native Compose tabs + StemJSON in the same app, sharing state through a
 * single [StemRuntime] instance.
 *
 *   - Home tab → native device cards. Toggling a card calls
 *     `runtime.trigger("deviceToggled", { id, name, on, timestamp })` —
 *     the JSON module's `onCustom` handler receives the payload and
 *     updates its state.
 *   - Rooms tab → embedded StemJSON module (the bundled `home.json`)
 *     rendered through Stem's Compose host. The same toggles surfaced as
 *     native cards drive this view too.
 *   - Activity tab → native event log. Subscribes to the module's
 *     `recentEvents` state key via `runtime.subscribe` and re-renders.
 *
 * Demonstrates: zero serialization between sides, bidirectional state
 * flow, single source of truth for "what's on" across native + JSON.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StemHomeApp()
                }
            }
        }
    }
}

@Composable
private fun StemHomeApp() {
    val context = LocalContext.current
    val runtime = remember(context) {
        StemRuntime(context.applicationContext as Application).navigationEmbedded()
    }

    val devicesFlow = remember { MutableStateFlow<List<Device>>(emptyList()) }
    val activityFlow = remember { MutableStateFlow<List<ActivityEntry>>(emptyList()) }
    val activeCountFlow = remember { MutableStateFlow(0) }

    var render by remember { mutableStateOf<StemRender?>(null) }
    var selectedTab by remember { mutableStateOf(1) } // start on Rooms so the JSON mounts

    LaunchedEffect(Unit) {
        val bytes = context.assets.open("json/home.json").use { it.readBytes() }
        val outcome = runtime.validate(bytes)
        if (outcome is StemValidationOutcome.Success) {
            render = outcome.render
            // `runtime.subscribe(key, render)` emits whenever the named
            // state key in the module's state flips. Each side observes
            // a different slice — devices for the native grid, activity
            // for the native log, activeCount as a compact summary.
            runtime.subscribe("devices", outcome.render) { value ->
                devicesFlow.value = parseDevices(value)
            }
            runtime.subscribe("activeCount", outcome.render) { value ->
                activeCountFlow.value = (value as? Number)?.toInt() ?: 0
            }
            runtime.subscribe("recentEvents", outcome.render) { value ->
                activityFlow.value = parseActivity(value)
            }
        }
    }

    val devices by devicesFlow.collectAsState()
    val activity by activityFlow.collectAsState()
    val activeCount by activeCountFlow.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.MeetingRoom, contentDescription = null) },
                    label = { Text("Rooms") },
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text("Activity") },
                )
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    devices = devices,
                    activeCount = activeCount,
                    onToggle = { device ->
                        // Fire the bridge event the JSON module's
                        // onCustom handler listens for.
                        runtime.trigger(
                            "deviceToggled",
                            mapOf(
                                "id" to device.id,
                                "name" to device.name,
                                "on" to !device.on,
                                "timestamp" to (System.currentTimeMillis() / 1000.0),
                            ),
                        )
                    },
                    paddingValues = padding,
                )
                1 -> RoomsScreen(render = render, paddingValues = padding)
                2 -> ActivityScreen(entries = activity, paddingValues = padding)
            }
        }
    }
}
