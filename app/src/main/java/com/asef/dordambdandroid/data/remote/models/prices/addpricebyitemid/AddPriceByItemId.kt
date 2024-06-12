package com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid


import com.google.gson.annotations.SerializedName

data class AddPriceByItemId(
    @SerializedName("item")
    val item: Item = Item(),
    @SerializedName("price")
    val price: Float = 0.0f
)