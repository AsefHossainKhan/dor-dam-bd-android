package com.asef.dordambdandroid.repository

import com.asef.dordambdandroid.data.remote.DorDamBDAPI
import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.data.remote.models.items.edititem.EditItem
import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.AddPriceByItemId
import com.asef.dordambdandroid.data.remote.models.prices.editprice.EditPrice
import com.asef.dordambdandroid.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@ActivityScoped
class DorDamBDRepository @Inject constructor(
    private val dorDamBDAPI: DorDamBDAPI
){
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

}