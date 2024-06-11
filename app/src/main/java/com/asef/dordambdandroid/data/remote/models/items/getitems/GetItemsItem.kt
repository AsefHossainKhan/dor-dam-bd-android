package com.asef.dordambdandroid.data.remote.models.items.getitems


import com.google.gson.annotations.SerializedName

data class GetItemsItem(
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("created_by")
    val createdBy: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("updated_at")
    val updatedAt: String = ""
)