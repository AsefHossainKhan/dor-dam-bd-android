package com.asef.dordambdandroid.data.remote.models.prices.location

import com.google.gson.stream.JsonWriter

// SPEC: spec-5f5a30, spec-14240b, spec-628f81

/**
 * The optional location fields of POST /prices and PATCH /prices/:id.
 *
 * The backend treats them as one unit: sending any of them sets the whole
 * location, and all-null removes it. So a request either carries all four
 * (nulls written out explicitly) or none, which leaves the location as it is.
 */
data class PriceLocationFields(
    val placeId: Int? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val locationSource: String? = null,
) {
    companion object {
        /** Removes a price's location. */
        val NONE = PriceLocationFields()
    }
}

/** Writes all four location fields, nulls included, whatever the writer's null setting. */
internal fun JsonWriter.writeLocation(location: PriceLocationFields) {
    val previous = serializeNulls
    serializeNulls = true
    name("place")
    if (location.placeId == null) nullValue() else value(location.placeId.toLong())
    name("lat")
    if (location.lat == null) nullValue() else value(location.lat)
    name("lng")
    if (location.lng == null) nullValue() else value(location.lng)
    name("location_source")
    if (location.locationSource == null) nullValue() else value(location.locationSource)
    serializeNulls = previous
}
