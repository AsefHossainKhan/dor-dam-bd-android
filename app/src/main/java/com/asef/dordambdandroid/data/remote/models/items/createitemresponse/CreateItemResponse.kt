package com.asef.dordambdandroid.data.remote.models.items.createitemresponse


import com.google.gson.annotations.SerializedName

data class CreateItemResponse(
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("updated_at")
    val updatedAt: String = ""
)