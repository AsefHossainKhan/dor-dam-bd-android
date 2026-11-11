package com.asef.dordambdandroid.location

import android.content.SharedPreferences
import com.asef.dordambdandroid.data.remote.models.places.PlacePathStep
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// SPEC: spec-5f5a30 — the last location carries over to the next entry
// for 2 hours, or until the device has moved more than about 1 km.

const val STICKY_MAX_AGE_MS: Long = 2 * 60 * 60 * 1000
const val STICKY_MAX_DISTANCE_M: Double = 1_000.0

/** The location of the last saved price, and where the device was then. */
data class StickyLocation(
    val location: ChosenLocation,
    val savedAtMs: Long,
    val savedFix: GeoPoint?,
) {
    /**
     * Still usable at [nowMs] with the device at [currentFix]: under 2 hours old
     * and, when both positions are known, within about 1 km of where it was saved.
     */
    fun isValid(nowMs: Long, currentFix: GeoPoint?): Boolean {
        val age = nowMs - savedAtMs
        if (age < 0 || age > STICKY_MAX_AGE_MS) return false
        if (savedFix != null && currentFix != null &&
            distanceMetres(savedFix, currentFix) > STICKY_MAX_DISTANCE_M
        ) return false
        return true
    }

    /** The location as pre-filled into the next sheet: carried over automatically. */
    fun carriedOver(): ChosenLocation = location.copy(source = LocationSource.AUTO)
}

/** Great-circle distance in metres. */
fun distanceMetres(a: GeoPoint, b: GeoPoint): Double {
    val r = 6_371_000.0
    val dLat = Math.toRadians(b.lat - a.lat)
    val dLng = Math.toRadians(b.lng - a.lng)
    val h = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLng / 2).pow(2)
    return 2 * r * asin(sqrt(h))
}

/** Keeps the sticky location in SharedPreferences. */
@Singleton
class StickyLocationStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    private val gson = Gson()

    private data class Stored(
        val path: List<PlacePathStep>? = null,
        val lat: Double? = null,
        val lng: Double? = null,
        val source: String? = null,
        val savedAtMs: Long = 0,
        val fixLat: Double? = null,
        val fixLng: Double? = null,
    )

    fun load(): StickyLocation? = try {
        prefs.getString(KEY, null)?.let { json ->
            val s = gson.fromJson(json, Stored::class.java)
            val source = LocationSource.fromWire(s.source)
            if (source == null) null
            else StickyLocation(
                location = ChosenLocation(s.path ?: emptyList(), s.lat, s.lng, source),
                savedAtMs = s.savedAtMs,
                savedFix = if (s.fixLat != null && s.fixLng != null) GeoPoint(s.fixLat, s.fixLng) else null,
            )
        }
    } catch (e: Exception) {
        null
    }

    /** [fix] is where the device was; without one, a pin's coordinates stand in. */
    fun save(location: ChosenLocation, nowMs: Long, fix: GeoPoint?) {
        val stored = Stored(
            path = location.path,
            lat = location.lat,
            lng = location.lng,
            source = location.source.wire,
            savedAtMs = nowMs,
            fixLat = fix?.lat ?: location.lat,
            fixLng = fix?.lng ?: location.lng,
        )
        prefs.edit().putString(KEY, gson.toJson(stored)).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    /** Whether the location permission has been asked for once already. */
    var permissionAsked: Boolean
        get() = prefs.getBoolean(KEY_PERMISSION_ASKED, false)
        set(value) = prefs.edit().putBoolean(KEY_PERMISSION_ASKED, value).apply()

    private companion object {
        const val KEY = "STICKY_PRICE_LOCATION"
        const val KEY_PERMISSION_ASKED = "LOCATION_PERMISSION_ASKED"
    }
}
