package com.asef.dordambdandroid.data.remote

import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.data.remote.models.items.createitemresponse.CreateItemResponse
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.AddPriceByItemId
import com.asef.dordambdandroid.data.remote.models.prices.addpriceresponse.AddPriceResponse
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemId
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DorDamBDAPI {
    @GET("items")
    suspend fun getItems(): GetItems

    @GET("prices/items/{id}")
    suspend fun getPricesByItemId(
        @Path("id") itemId: Int,
    ): PriceByItemId

    @POST("items")
    suspend fun createItem(
        @Body createItem: CreateItem
    ): CreateItemResponse

    @POST("prices")
    suspend fun addPrice(
        @Body price: AddPriceByItemId
    ): AddPriceResponse
}