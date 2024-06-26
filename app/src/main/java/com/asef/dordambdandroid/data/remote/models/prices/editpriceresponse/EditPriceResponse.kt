package com.asef.dordambdandroid.data.remote.models.prices.editpriceresponse


import com.google.gson.annotations.SerializedName

data class EditPriceResponse(
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("price")
    val price: Int = 0,
    @SerializedName("updated_at")
    val updatedAt: String = ""
)