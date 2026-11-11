package com.asef.dordambdandroid.data.remote.models.places

import com.google.gson.annotations.SerializedName

// SPEC: spec-5f5a30, spec-14240b — shapes of the backend's /places endpoints.

/** One step of a place path, e.g. Tejgaon (area). */
data class PlacePathStep(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("level")
    val level: String = "",
)

/** A shared place as /places returns it. `path` runs fine to coarse, starting with the place. */
data class PlaceView(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("level")
    val level: String = "",
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lng")
    val lng: Double? = null,
    @SerializedName("path")
    val path: List<PlacePathStep>? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("distance_m")
    val distanceM: Int? = null,
) {
    /** The path, falling back to the place alone when the server sent none. */
    val pathOrSelf: List<PlacePathStep>
        get() = path?.takeIf { it.isNotEmpty() } ?: listOf(PlacePathStep(id, name, level))
}

/** GET /places/nearby: shared places within 500 m, and the OSM path (null when OSM failed). */
data class NearbyPlaces(
    @SerializedName("places")
    val places: List<PlaceView> = emptyList(),
    @SerializedName("osm")
    val osm: PlaceView? = null,
)

/** POST /places body. */
data class CreatePlace(
    @SerializedName("name")
    val name: String,
    @SerializedName("level")
    val level: String? = null,
    @SerializedName("parent_id")
    val parentId: Int? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lng")
    val lng: Double? = null,
)
