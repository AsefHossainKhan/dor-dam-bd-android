package com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid


import com.asef.dordambdandroid.data.remote.models.places.PlaceView
import com.google.gson.annotations.SerializedName

data class PriceByItemIdItem(
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("price")
    val price: Float = 0f,
    @SerializedName("updated_at")
    val updatedAt: String = "",
    // SPEC: spec-628f81 — the place path when one is set; the API never returns coordinates.
    @SerializedName("place")
    val place: PlaceView? = null,
    @SerializedName("location_source")
    val locationSource: String? = null,
)
