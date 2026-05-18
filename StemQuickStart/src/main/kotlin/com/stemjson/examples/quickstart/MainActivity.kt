package com.stemjson.examples.quickstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.stemjson.examples.json.JSONCatalog
import com.stemjson.runtime.StemRender
import com.stemjson.runtime.StemRuntime
import com.stemjson.runtime.StemValidationOutcome

/**
 * Smallest possible host integration. Validates one JSON module via
 * [StemRuntime] and embeds the resulting [StemRender] into the Compose
 * hierarchy. Copy this file as the starting point for a new host app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuickStartHost()
                }
            }
        }
    }
}

@Composable
private fun QuickStartHost() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val runtime = remember { StemRuntime(context) }
    var render by remember { mutableStateOf<StemRender?>(null) }

    LaunchedEffect(Unit) {
        val bytes = JSONCatalog.data(context, JSONCatalog.Module.HELLO)
        val outcome = runtime.validate(bytes)
        if (outcome is StemValidationOutcome.Success) render = outcome.render
    }

    val current = render
    if (current != null) {
        current.Render()
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
