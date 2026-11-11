package com.asef.dordambdandroid.location

import com.asef.dordambdandroid.data.remote.models.places.PlacePathStep
import com.asef.dordambdandroid.data.remote.models.places.PlaceView
import com.asef.dordambdandroid.data.remote.models.prices.location.PriceLocationFields

// SPEC: spec-5f5a30, spec-14240b, spec-628f81

/** How a price's location was set (the backend's location_source). */
enum class LocationSource(val wire: String) {
    AUTO("auto"),
    SUGGESTED("suggested"),
    TYPED("typed"),
    PINNED("pinned");

    companion object {
        fun fromWire(value: String?): LocationSource? = entries.firstOrNull { it.wire == value }
    }
}

/** A latitude/longitude pair. */
data class GeoPoint(val lat: Double, val lng: Double)

/**
 * The location chosen in a price sheet.
 *
 * [path] runs fine to coarse and starts with the place the price records.
 * Coordinates are kept only while the finest level is kept, or for a pin:
 * trimming to a coarser level drops them.
 */
data class ChosenLocation(
    val path: List<PlacePathStep>,
    val lat: Double? = null,
    val lng: Double? = null,
    val source: LocationSource,
) {
    val placeId: Int? get() = path.firstOrNull()?.id

    val hasCoordinates: Boolean get() = lat != null && lng != null

    /** e.g. "Karwan Bazar › Tejgaon › Dhaka". */
    val label: String get() = path.joinToString(" › ") { it.name }

    /** Keeps the path from [index] on (0 = finest). Any coarser level drops the coordinates. */
    fun trimTo(index: Int): ChosenLocation {
        if (index <= 0 || index >= path.size) return this
        return copy(path = path.drop(index), lat = null, lng = null)
    }

    fun toFields(): PriceLocationFields = PriceLocationFields(
        placeId = placeId,
        lat = if (hasCoordinates) lat else null,
        lng = if (hasCoordinates) lng else null,
        locationSource = source.wire,
    )

    companion object {
        /** A suggestion tapped in the add sheet: the place, with the device fix while it is the finest level. */
        fun suggested(place: PlaceView, fix: GeoPoint?) = ChosenLocation(
            path = place.pathOrSelf,
            lat = fix?.lat,
            lng = fix?.lng,
            source = LocationSource.SUGGESTED,
        )

        /** A shared place found or created by typing; the price gets no coordinates. */
        fun typed(place: PlaceView) = ChosenLocation(
            path = place.pathOrSelf,
            source = LocationSource.TYPED,
        )

        /** A dropped pin: its exact coordinates, with the place it resolved to, if any. */
        fun pinned(place: PlaceView?, pin: GeoPoint) = ChosenLocation(
            path = place?.pathOrSelf ?: emptyList(),
            lat = pin.lat,
            lng = pin.lng,
            source = LocationSource.PINNED,
        )

        /** The location an existing price already has (the API never returns its coordinates). */
        fun ofExistingPrice(place: PlaceView?, source: String?): ChosenLocation? {
            val locationSource = LocationSource.fromWire(source)
            if (place == null && locationSource == null) return null
            return ChosenLocation(
                path = place?.pathOrSelf ?: emptyList(),
                source = locationSource ?: LocationSource.TYPED,
            )
        }
    }
}

/**
 * A place path shortened for a price row, e.g. "Karwan Bazar, Dhaka":
 * the place itself and its city (or, without a city level, its coarsest level).
 */
fun shortPlaceLabel(path: List<PlacePathStep>): String {
    if (path.isEmpty()) return ""
    val first = path.first()
    if (path.size == 1) return first.name
    val coarse = path.drop(1).firstOrNull { it.level == "city" } ?: path.last()
    return if (coarse.name.equals(first.name, ignoreCase = true)) first.name
    else "${first.name}, ${coarse.name}"
}

/**
 * The two or three suggestion chips for the add sheet: the OSM path first
 * (the finest description of where the user stands), then the nearest shared places.
 */
fun buildSuggestions(osm: PlaceView?, shared: List<PlaceView>, max: Int = 3): List<PlaceView> {
    val result = mutableListOf<PlaceView>()
    if (osm != null) result += osm
    for (place in shared) {
        if (result.size >= max) break
        if (result.none { it.id == place.id }) result += place
    }
    return result.take(max)
}
