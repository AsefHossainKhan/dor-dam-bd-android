package com.asef.dordambdandroid.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// SPEC: spec-5f5a30 — fine location when granted, the coarse fix when only
// approximate is granted, and nothing (silently) when there is no fix.

/** Reads the device position through the platform LocationManager (no Play services, no key). */
@Singleton
class DeviceLocation @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasFinePermission(): Boolean = granted(Manifest.permission.ACCESS_FINE_LOCATION)

    fun hasAnyPermission(): Boolean =
        hasFinePermission() || granted(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * The current position, or null when there is no permission, no enabled
     * provider or no fix within [timeoutMs]. A recent last-known fix is used first.
     */
    @SuppressLint("MissingPermission")
    suspend fun currentFix(timeoutMs: Long = 10_000): GeoPoint? {
        val lm = manager ?: return null
        if (!hasAnyPermission()) return null
        return try {
            val providers = providersInOrder(lm)
            if (providers.isEmpty()) return null
            val recent = providers
                .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
                .filter { System.currentTimeMillis() - it.time < RECENT_FIX_MS }
                .minByOrNull { it.accuracy }
            if (recent != null) return recent.toGeoPoint()
            // The fused provider (Android 12+) blends GPS and network itself, and hands out
            // a coarsened fix when only approximate is granted: give it the whole budget.
            // Without it, try each remaining provider in turn.
            val ask = if (FUSED in providers) listOf(FUSED) else providers
            val perProvider = timeoutMs / ask.size
            ask.firstNotNullOfOrNull { provider ->
                withTimeoutOrNull(perProvider) { requestFix(lm, provider) }
            }?.toGeoPoint()
        } catch (e: SecurityException) {
            null
        }
    }

    /** With only approximate location granted, GPS is off limits and the coarse fix is used. */
    private fun providersInOrder(lm: LocationManager): List<String> {
        val wanted = if (hasFinePermission()) {
            listOf(FUSED, LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        } else {
            listOf(FUSED, LocationManager.NETWORK_PROVIDER)
        }
        val available = lm.getProviders(true)
        return wanted.filter { it in available }
    }

    /** One fresh fix: high accuracy when precise location is granted, balanced (coarse) otherwise. */
    @SuppressLint("MissingPermission")
    private suspend fun requestFix(lm: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { cont ->
            val executor = ContextCompat.getMainExecutor(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val signal = android.os.CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                val request = LocationRequest.Builder(0L)
                    .setQuality(
                        if (hasFinePermission()) LocationRequest.QUALITY_HIGH_ACCURACY
                        else LocationRequest.QUALITY_BALANCED_POWER_ACCURACY
                    )
                    .build()
                lm.getCurrentLocation(provider, request, signal, executor) { location ->
                    if (cont.isActive) cont.resume(location)
                }
            } else {
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                LocationManagerCompat.getCurrentLocation(lm, provider, signal, executor) { location ->
                    if (cont.isActive) cont.resume(location)
                }
            }
        }

    private fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

    private companion object {
        const val FUSED = "fused"
        const val RECENT_FIX_MS = 2 * 60 * 1000L
    }
}
