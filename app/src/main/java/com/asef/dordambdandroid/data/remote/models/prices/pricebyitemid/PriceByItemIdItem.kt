package com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid


import com.google.gson.annotations.SerializedName

data class PriceByItemIdItem(
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("price")
    val price: Int = 0,
    @SerializedName("updated_at")
    val updatedAt: String = ""
)