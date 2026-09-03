package com.asef.dordambdandroid.ui.screens.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.data.remote.models.items.edititem.EditItem
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItemsItem
import com.asef.dordambdandroid.data.remote.models.items.summary.ItemSummary
import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.AddPriceByItemId
import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.Item
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemId
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemIdItem
import com.asef.dordambdandroid.repository.DorDamBDRepository
import com.asef.dordambdandroid.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.xdrop.fuzzywuzzy.FuzzySearch
import timber.log.Timber
import javax.inject.Inject

enum class PriceTrend { UP, DOWN, STABLE, UNKNOWN }

data class ItemPriceInfo(
    val latestPrice: PriceByItemIdItem? = null,
    val trend: PriceTrend = PriceTrend.UNKNOWN
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dorDamBDRepository: DorDamBDRepository
) : ViewModel() {
    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private var _hasError = MutableStateFlow(false)
    val hasError = _hasError.asStateFlow()
    private var _error = MutableStateFlow("")
    val error = _error.asStateFlow()
    private var _itemList = MutableStateFlow<List<GetItemsItem>>(emptyList())
    val itemList = _itemList.asStateFlow()

    private var _editBottomSheetVisibility = MutableStateFlow(false)
    val editBottomSheetVisibility = _editBottomSheetVisibility.asStateFlow()
    private var _itemText = MutableStateFlow("")
    val itemText = _itemText.asStateFlow()
    private var _itemId = MutableStateFlow(0)
    val itemId = _itemId.asStateFlow()
    private var _itemTextError = MutableStateFlow("")
    val itemTextError = _itemTextError.asStateFlow()

    private var _originalItemsList = listOf<GetItemsItem>()

    private var _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // Combined price info per item — single emission keeps recompositions minimal
    private var _itemPriceInfo = MutableStateFlow<Map<Int, ItemPriceInfo>>(emptyMap())
    val itemPriceInfo = _itemPriceInfo.asStateFlow()

    // Quick-add price sheet state (triggered from home screen card)
    private var _quickAddSheetVisible = MutableStateFlow(false)
    val quickAddSheetVisible = _quickAddSheetVisible.asStateFlow()
    private var _quickAddItemId = MutableStateFlow(0)
    val quickAddItemId = _quickAddItemId.asStateFlow()
    private var _quickAddItemName = MutableStateFlow("")
    val quickAddItemName = _quickAddItemName.asStateFlow()

    fun openEditBottomSheet(open: Boolean) {
        _editBottomSheetVisibility.value = open
    }

    fun setItemText(text: String) {
        _itemText.value = text
    }

    fun setItemId(id: Int) {
        _itemId.value = id
    }

    fun openQuickAddSheet(itemId: Int, itemName: String) {
        _quickAddItemId.value = itemId
        _quickAddItemName.value = itemName
        _quickAddSheetVisible.value = true
    }

    fun closeQuickAddSheet() {
        _quickAddSheetVisible.value = false
    }

    // REQ: spec-90d023 — filter items by improved fuzzy name search
    fun changeSearchText(text: String) {
        if (text.isEmpty()) {
            _itemList.value = _originalItemsList
        }

        _searchText.value = text

        if (_searchText.value.isNotEmpty()) {
            val output = _originalItemsList
                .asSequence()
                .map { it to FuzzySearch.weightedRatio(it.name, _searchText.value) }
                .sortedByDescending { it.second }
                .filter { it.second != 0 }
                .take(20)
                .map { it.first }
                .toList()

            _itemList.value = output.distinct()
        }
    }

    private fun clearSearchText() {
        changeSearchText("")
    }

    fun getItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dorDamBDRepository.getItemsSummary()
            response.catch {
                Timber.e("Error $this")
            }.collect {
                when (it) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }

                    is Resource.Error -> {
                        _isLoading.value = false
                        _hasError.value = true
                        _itemList.value = emptyList()
                        _itemPriceInfo.value = emptyMap()
                        _error.value = it.errorMessage.toString()
                    }

                    is Resource.Success -> {
                        _isLoading.value = false
                        _hasError.value = false
                        _error.value = ""

                        val summaries = it.data!!
                        clearSearchText()

                        // Derive items list for display and search
                        val items = summaries.map { s ->
                            GetItemsItem(
                                id = s.id,
                                name = s.name,
                                updatedAt = s.updatedAt,
                                createdAt = s.updatedAt,
                                createdBy = null
                            )
                        }
                        _itemList.value = items
                        _originalItemsList = items

                        // Derive price info in the same pass — no separate network calls
                        _itemPriceInfo.value = summaries.associate { s ->
                            s.id to ItemPriceInfo(
                                latestPrice = s.latestPrice?.let { price ->
                                    PriceByItemIdItem(
                                        id = 0,
                                        price = price,
                                        createdAt = s.latestPriceDate ?: "",
                                        updatedAt = s.updatedAt
                                    )
                                },
                                trend = when {
                                    s.latestPrice == null || s.prevPrice == null -> PriceTrend.UNKNOWN
                                    s.latestPrice > s.prevPrice -> PriceTrend.UP
                                    s.latestPrice < s.prevPrice -> PriceTrend.DOWN
                                    else -> PriceTrend.STABLE
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun addPrice(itemId: Int, price: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dorDamBDRepository.addPriceByItemId(
                AddPriceByItemId(item = Item(itemId), price = price)
            )
            response.catch { Timber.e("Error $this") }
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {}
                        is Resource.Error -> {
                            _hasError.value = true
                            _error.value = resource.errorMessage.toString()
                        }
                        is Resource.Success -> {
                            _hasError.value = false
                            _error.value = ""
                            refreshLatestPriceForItem(itemId)
                        }
                    }
                }
        }
    }

    private suspend fun refreshLatestPriceForItem(itemId: Int) {
        try {
            val prices = dorDamBDRepository.getLatestPricesForItem(itemId) ?: return
            val updated = _itemPriceInfo.value.toMutableMap()
            updated[itemId] = ItemPriceInfo(
                latestPrice = prices.firstOrNull(),
                trend = when {
                    prices.size < 2 -> PriceTrend.UNKNOWN
                    prices[0].price > prices[1].price -> PriceTrend.UP
                    prices[0].price < prices[1].price -> PriceTrend.DOWN
                    else -> PriceTrend.STABLE
                }
            )
            _itemPriceInfo.value = updated
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing price for item $itemId")
        }
    }

    fun postItem(item: CreateItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dorDamBDRepository.createItem(item)
            response.catch {
                Timber.e("Error $this")
            }.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }

                    is Resource.Error -> {
                        _isLoading.value = false
                        _hasError.value = true
                        _error.value = resource.errorMessage.toString()
                    }

                    is Resource.Success -> {
                        _isLoading.value = false
                        _hasError.value = false
                        _error.value = ""

                        getItems()
                    }
                }
            }
        }
    }

    fun editItem(id: Int, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dorDamBDRepository.editItem(id, EditItem(name))
            response.catch {
                Timber.e("Error $this")
            }.collect {
                when (it) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }

                    is Resource.Error -> {
                        _isLoading.value = false
                        _hasError.value = true
                        _error.value = it.errorMessage.toString()
                    }

                    is Resource.Success -> {
                        _isLoading.value = false
                        _hasError.value = false
                        _error.value = ""

                        getItems()
                    }
                }
            }
        }
    }
}