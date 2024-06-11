package com.asef.dordambdandroid.data.remote

import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import retrofit2.http.GET

interface DorDamBDAPI {
    @GET("items")
    suspend fun getItems(): GetItems
}