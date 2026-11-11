package com.asef.dordambdandroid.data.remote

import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.data.remote.models.items.createitemresponse.CreateItemResponse
import com.asef.dordambdandroid.data.remote.models.items.edititem.EditItem
import com.asef.dordambdandroid.data.remote.models.items.edititemresponse.EditItemResponse
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import com.asef.dordambdandroid.data.remote.models.items.summary.ItemSummary
import com.asef.dordambdandroid.data.remote.models.places.CreatePlace
import com.asef.dordambdandroid.data.remote.models.places.NearbyPlaces
import com.asef.dordambdandroid.data.remote.models.places.PlaceView
import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.AddPriceByItemId
import com.asef.dordambdandroid.data.remote.models.prices.addpriceresponse.AddPriceResponse
import com.asef.dordambdandroid.data.remote.models.prices.editprice.EditPrice
import com.asef.dordambdandroid.data.remote.models.prices.editpriceresponse.EditPriceResponse
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemId
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DorDamBDAPI {
    @GET("items")
    suspend fun getItems(): GetItems

    @GET("items/summary")
    suspend fun getItemsSummary(): List<ItemSummary>

    @PATCH("items/{id}")
    suspend fun editItem(
        @Path("id") itemId: Int,
        @Body editItem: EditItem
    ): EditItemResponse

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

    @PATCH("prices/{id}")
    suspend fun editPrice(
        @Path("id") priceId: Int,
        @Body price: EditPrice
    ): EditPriceResponse

    // SPEC: spec-5f5a30, spec-14240b — places; all OSM traffic stays on the backend.
    @GET("places/nearby")
    suspend fun getNearbyPlaces(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): NearbyPlaces

    @GET("places/search")
    suspend fun searchPlaces(
        @Query("q") query: String,
    ): List<PlaceView>

    @POST("places")
    suspend fun createPlace(
        @Body place: CreatePlace
    ): PlaceView
}
