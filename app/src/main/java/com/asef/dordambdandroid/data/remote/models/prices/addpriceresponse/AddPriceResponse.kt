package com.asef.dordambdandroid.data.remote.models.prices.addpriceresponse


import com.google.gson.annotations.SerializedName

data class AddPriceResponse(
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("item")
    val item: Item = Item(),
    @SerializedName("price")
    val price: Int = 0,
    @SerializedName("updated_at")
    val updatedAt: String = ""
)