package com.stemjson.examples.compose

import android.Manifest
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stemjson.examples.json.JSONCatalog
import com.stemjson.runtime.StemRender
import com.stemjson.runtime.StemRuntime
import com.stemjson.runtime.StemValidationOutcome
import kotlinx.coroutines.launch

/**
 * Full showcase host: a grid of every module the StemJSON catalog ships.
 * Tap a tile to open the module in a full-screen modal that mounts the
 * rendered StemRender. The modal closes via the module's own `onClose`
 * state key, streamed back through `runtime.stream("onClose", render)`.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StemExamplesApp()
                }
            }
        }
    }
}

private data class AppEntry(
    val module: JSONCatalog.Module,
    val render: StemRender? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StemExamplesApp() {
    val context = LocalContext.current
    val runtime = remember(context) { StemRuntime(context.applicationContext as Application) }

    // Location permission gate for the SDK's built-in StemServiceType.LOCATION.
    // The system prompt fires the first time the user opens the showcase;
    // later launches resolve from the cached grant. The launcher's callback
    // is intentionally a no-op — StemLocationService re-checks
    // `ContextCompat.checkSelfPermission` on every action invocation, so
    // any grant takes effect on the next "Use my location" tap without
    // additional plumbing.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* permission state observed at action-fire time via ContextCompat */ }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    var entries by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var openEntry by remember { mutableStateOf<AppEntry?>(null) }

    LaunchedEffect(Unit) {
        // Validate every module at startup so the grid only renders entries
        // the runtime accepts.
        val validated = JSONCatalog.Module.displayOrder.mapNotNull { module ->
            val bytes = try {
                JSONCatalog.data(context, module)
            } catch (_: Exception) {
                return@mapNotNull null
            }
            (runtime.validate(bytes) as? StemValidationOutcome.Success)
                ?.let { AppEntry(module, it.render) }
        }
        entries = validated
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stem Examples", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(entries, key = { it.module.rawValue }) { entry ->
                ModuleTile(
                    icon = iconFor(entry.module),
                    title = entry.module.displayName,
                    subtitle = entry.module.subtitle,
                    tint = colorFor(entry.module),
                    onTap = { openEntry = entry },
                )
            }
        }
    }

    val opened = openEntry
    if (opened?.render != null) {
        ModuleSheet(
            runtime = runtime,
            entry = opened,
            onDismiss = { openEntry = null },
        )
    }
}

@Composable
private fun ModuleSheet(
    runtime: StemRuntime,
    entry: AppEntry,
    onDismiss: () -> Unit,
) {
    val render = entry.render ?: return
    val scope = rememberCoroutineScope()

    // runtime.stream(key, render) emits every change of the module's
    // `onClose` state value; a `true` flip kills the render and dismisses
    // the modal.
    LaunchedEffect(render.unitId) {
        scope.launch {
            runtime.stream("onClose", render).collect { value ->
                if (value == true) {
                    runtime.kill(render)
                    onDismiss()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            render.Render()
        }
    }
}

@Composable
private fun ModuleTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onTap: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.height(36.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 12.sp,
            )
        }
    }
}

private fun iconFor(module: JSONCatalog.Module): ImageVector = when (module) {
    JSONCatalog.Module.CALCULATOR -> Icons.Filled.Calculate
    JSONCatalog.Module.MESSENGER -> Icons.Filled.Forum
    JSONCatalog.Module.WEATHER -> Icons.Filled.WbCloudy
    JSONCatalog.Module.SHOP -> Icons.Filled.ShoppingBag
    JSONCatalog.Module.GALLERY -> Icons.Filled.PhotoLibrary
    JSONCatalog.Module.RECIPES -> Icons.Filled.MenuBook
    JSONCatalog.Module.BOOKING -> Icons.Filled.Event
    JSONCatalog.Module.SKELETONS -> Icons.Filled.ViewQuilt
    JSONCatalog.Module.INSTAGRAM -> Icons.Filled.Camera
    JSONCatalog.Module.FUNCTIONS -> Icons.Filled.Functions
    JSONCatalog.Module.SYSTEM -> Icons.Filled.Smartphone
    JSONCatalog.Module.MEDIA -> Icons.Filled.PlayCircle
    JSONCatalog.Module.SPLITVIEW -> Icons.Filled.Splitscreen
    JSONCatalog.Module.HELLO -> Icons.Filled.WavingHand
}

private fun colorFor(module: JSONCatalog.Module): Color = when (module.iconColor) {
    "orange" -> Color(0xFFFF9500)
    "purple" -> Color(0xFFAF52DE)
    "blue" -> Color(0xFF007AFF)
    "green" -> Color(0xFF34C759)
    "teal" -> Color(0xFF30B0C7)
    "indigo" -> Color(0xFF5856D6)
    "pink" -> Color(0xFFFF2D55)
    "brown" -> Color(0xFFA2845E)
    "red" -> Color(0xFFFF3B30)
    "mint" -> Color(0xFF00C7BE)
    "cyan" -> Color(0xFF32ADE6)
    "gray" -> Color(0xFF8E8E93)
    "yellow" -> Color(0xFFFFCC00)
    else -> Color.Unspecified
}

