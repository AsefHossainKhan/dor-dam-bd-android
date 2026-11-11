package com.asef.dordambdandroid.repository

import com.asef.dordambdandroid.data.remote.DorDamBDAPI
import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.data.remote.models.items.edititem.EditItem
import com.asef.dordambdandroid.data.remote.models.items.summary.ItemSummary
import com.asef.dordambdandroid.data.remote.models.places.CreatePlace
import com.asef.dordambdandroid.data.remote.models.places.NearbyPlaces
import com.asef.dordambdandroid.data.remote.models.places.PlaceView
import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.AddPriceByItemId
import com.asef.dordambdandroid.data.remote.models.prices.editprice.EditPrice
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemId
import com.asef.dordambdandroid.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@ActivityScoped
class DorDamBDRepository @Inject constructor(
    private val dorDamBDAPI: DorDamBDAPI
){
    suspend fun getItemsSummary() =
        flow {
            emit(Resource.Loading())
            try {
                val result = withTimeout(10_000) {
                    dorDamBDAPI.getItemsSummary()
                }
                emit(Resource.Success(result))
            } catch (exception: Exception) {
                emit(Resource.Error(errorMessage = exception.message ?: "An unknown exception occurred"))
            }
        }

    suspend fun getItems() =
        flow {
            emit(Resource.Loading())
            try {
                val result = withTimeout(10_000) {
                    dorDamBDAPI.getItems()
                }
                emit(Resource.Success(result))
            } catch (exception: Exception) {
                emit(Resource.Error(errorMessage = exception.message ?: "An unknown exception occurred"))
            }
        }

    suspend fun createItem(item: CreateItem) =
        flow {
            emit(Resource.Loading())
            try {
                val result = withTimeout(10_000) {
                    dorDamBDAPI.createItem(item)
                }
                emit(Resource.Success(result))
            } catch (exception: Exception) {
                emit(Resource.Error(errorMessage = exception.message ?: "An unknown exception occurred"))
            }
        }

    suspend fun getPricesByItemId(itemId: Int) =
        flow {
            emit(Resource.Loading())
            try {
                val result = withTimeout(10_000) {
                    dorDamBDAPI.getPricesByItemId(itemId)
                }
                emit(Resource.Success(result))
            } catch (exception: Exception) {
                emit(Resource.Error(errorMessage = exception.message ?: "An unknown exception occurred"))
            }
        }

    suspend fun addPriceByItemId(addPriceByItemId: AddPriceByItemId) =
        flow {
            emit(Resource.Loading())
            try {
                val result = withTimeout(10_000) {
                    dorDamBDAPI.addPrice(addPriceByItemId)
                }
                emit(Resource.Success(result))
            } catch (exception: Exception) {
                emit(Resource.Error(errorMessage = exception.message ?: "An unknown exception occurred"))
            }
        }

    suspend fun editItem(id: Int, item: EditItem) =
        flow {
            emit(Resource.Loading())
            try {
                val result = withTimeout(10_000) {
                    dorDamBDAPI.editItem(id, item)
                }
                emit(Resource.Success(result))
            } catch (exception: Exception) {
                emit(Resource.Error(errorMessage = exception.message ?: "An unknown exception occurred"))
            }
        }

    suspend fun editPrice(id: Int, price: EditPrice) =
        flow {
            emit(Resource.Loading())
            try {
                val result = withTimeout(10_000) {
                    dorDamBDAPI.editPrice(id, price)
                }
                emit(Resource.Success(result))
            } catch (exception: Exception) {
                emit(Resource.Error(errorMessage = exception.message ?: "An unknown exception occurred"))
            }
        }

    suspend fun getLatestPricesForItem(itemId: Int): PriceByItemId? =
        try {
            withTimeout(10_000) { dorDamBDAPI.getPricesByItemId(itemId) }
        } catch (e: Exception) { null }

    // SPEC: spec-5f5a30 — null on any failure: suggestions are skipped silently.
    suspend fun getNearbyPlaces(lat: Double, lng: Double): NearbyPlaces? =
        try {
            withTimeout(10_000) { dorDamBDAPI.getNearbyPlaces(lat, lng) }
        } catch (e: Exception) { null }

    // SPEC: spec-14240b
    suspend fun searchPlaces(query: String): Resource<List<PlaceView>> =
        try {
            Resource.Success(withTimeout(10_000) { dorDamBDAPI.searchPlaces(query) })
        } catch (e: Exception) {
            Resource.Error(errorMessage = e.message ?: "An unknown exception occurred")
        }

    // SPEC: spec-14240b
    suspend fun createPlace(place: CreatePlace): Resource<PlaceView> =
        try {
            Resource.Success(withTimeout(10_000) { dorDamBDAPI.createPlace(place) })
        } catch (e: Exception) {
            Resource.Error(errorMessage = e.message ?: "An unknown exception occurred")
        }
}
