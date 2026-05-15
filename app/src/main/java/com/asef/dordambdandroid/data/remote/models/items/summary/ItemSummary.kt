package com.asef.dordambdandroid.data.remote.models.items.summary

import com.google.gson.annotations.SerializedName

data class ItemSummary(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("updatedAt")
    val updatedAt: String = "",
    @SerializedName("latestPrice")
    val latestPrice: Float? = null,
    @SerializedName("prevPrice")
    val prevPrice: Float? = null,
    @SerializedName("latestPriceDate")
    val latestPriceDate: String? = null
)
