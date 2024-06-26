package com.asef.dordambdandroid.data.remote.models.prices.editprice


import com.google.gson.annotations.SerializedName

data class EditPrice(
    @SerializedName("price")
    val price: Float = 0.0f
)