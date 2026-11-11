package com.asef.dordambdandroid.ui.components.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.asef.dordambdandroid.R
import com.asef.dordambdandroid.location.GeoPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView

// SPEC: spec-14240b — MapLibre with OpenFreeMap tiles: free, no API key and
// no billing account; OpenStreetMap attribution always visible.
const val OPEN_FREE_MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"

/** Where the map opens with no pin and no fix: central Dhaka. */
val DEFAULT_MAP_CENTER = GeoPoint(23.8103, 90.4125)

/**
 * "Pick on map": the user moves the map under a fixed centre pin, optionally
 * names the spot, and confirms. The pin's exact coordinates are kept.
 */
@Composable
fun MapPickerDialog(
    initial: GeoPoint?,
    resolving: Boolean,
    error: String?,
    onConfirm: (GeoPoint, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val start = initial ?: DEFAULT_MAP_CENTER
            var center by remember { mutableStateOf(start) }
            var name by rememberSaveable { mutableStateOf("") }

            Column(modifier = Modifier.imePadding()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    Text(stringResource(R.string.pick_on_map), style = MaterialTheme.typography.titleMedium)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LibreMap(
                        start = start,
                        onCenterChanged = { center = it },
                        modifier = Modifier.fillMaxSize()
                    )
                    // The pin's tip sits on the map centre.
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = stringResource(R.string.pin),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                            .offset(y = (-20).dp)
                    )
                    OsmAttribution(modifier = Modifier.align(Alignment.BottomEnd))
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.move_map_to_pin),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.name_this_place)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onConfirm(center, name.takeIf { it.isNotBlank() }) },
                        enabled = !resolving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (resolving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.use_this_spot))
                        }
                    }
                }
            }
        }
    }
}

/** The required OpenStreetMap credit, always on screen and linking to the licence page. */
@Composable
fun OsmAttribution(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = stringResource(R.string.osm_attribution),
        style = MaterialTheme.typography.labelSmall,
        color = Color.Black,
        modifier = modifier
            .padding(4.dp)
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            .clickable { uriHandler.openUri(OSM_COPYRIGHT_URL) }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun LibreMap(
    start: GeoPoint,
    onCenterChanged: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapLibre.getInstance(context)
        // Texture mode draws correctly inside a dialog window.
        MapView(context, MapLibreMapOptions.createFromAttributes(context).textureMode(true)).apply {
            onCreate(null)
            getMapAsync { map ->
                map.setStyle(OPEN_FREE_MAP_STYLE_URL)
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(start.lat, start.lng))
                    .zoom(16.0)
                    .build()
                map.uiSettings.isAttributionEnabled = true
                map.uiSettings.isRotateGesturesEnabled = false
                map.addOnCameraIdleListener {
                    map.cameraPosition.target?.let { onCenterChanged(GeoPoint(it.latitude, it.longitude)) }
                }
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        // Adding the observer replays ON_START / ON_RESUME up to the current state.
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    AndroidView(factory = { mapView }, modifier = modifier)
}
