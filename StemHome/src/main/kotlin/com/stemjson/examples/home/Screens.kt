package com.stemjson.examples.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stemjson.runtime.StemRender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Native Home tab — grid of device cards. Toggling a card calls `onToggle`
 * which fires `runtime.trigger("deviceToggled", …)` in the parent.
 */
@Composable
internal fun HomeScreen(
    devices: List<Device>,
    activeCount: Int,
    onToggle: (Device) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
    ) {
        SummaryHeader(activeCount = activeCount, total = devices.size)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(devices, key = { it.id }) { device ->
                DeviceCard(device = device, onToggle = { onToggle(device) })
            }
        }
    }
}

@Composable
private fun SummaryHeader(activeCount: Int, total: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "$activeCount of $total devices on",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Live from the JSON module's state",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(device: Device, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = device.room,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (device.on) "On" else "Off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (device.on) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = device.on, onCheckedChange = { onToggle() })
            }
        }
    }
}

/**
 * Rooms tab — hosts the StemJSON module. `navigationEmbedded()` on the
 * runtime means the module's own `navigation` component participates in
 * the host's nav stack (back gesture, etc.) instead of building its own.
 */
@Composable
internal fun RoomsScreen(render: StemRender?, paddingValues: PaddingValues) {
    if (render != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            render.Render()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Activity tab — native event log fed by `runtime.subscribe("recentEvents")`.
 * Demonstrates the JSON→native direction of the bridge.
 */
@Composable
internal fun ActivityScreen(entries: List<ActivityEntry>, paddingValues: PaddingValues) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(entries, key = { it.id }) { entry ->
            ActivityRow(entry = entry)
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntry) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${entry.deviceName} — ${entry.action}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = formatter.format(Date((entry.timestamp * 1000).toLong())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun stableMaterialColor(seed: String): Color {
    // Hash a string into a stable hue so room tints stay consistent
    // across renders without hard-coding a per-room palette.
    val hue = (seed.hashCode().toFloat() / Int.MAX_VALUE) * 360f
    return Color.hsl(if (hue < 0) hue + 360f else hue, 0.4f, 0.5f)
}
