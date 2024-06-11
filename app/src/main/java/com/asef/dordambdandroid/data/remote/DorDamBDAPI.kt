package com.asef.dordambdandroid.data.remote

import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemId
import retrofit2.http.GET
import retrofit2.http.Path

interface DorDamBDAPI {
    @GET("items")
    suspend fun getItems(): GetItems

    @GET("prices/items/:id}")
    suspend fun getPricesByItemId(
        @Path("id") itemId: Int,
    ): PriceByItemId
}