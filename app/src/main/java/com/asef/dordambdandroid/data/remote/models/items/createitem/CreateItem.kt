package com.asef.dordambdandroid.data.remote.models.items.createitem


import com.google.gson.annotations.SerializedName

data class CreateItem(
    @SerializedName("name")
    val name: String = ""
)