package com.asef.dordambdandroid.ui.screens.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.data.remote.models.items.edititem.EditItem
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItemsItem
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
    private var _itemList = MutableStateFlow(ArrayList<GetItemsItem>())
    val itemList = _itemList.asStateFlow()

    private var _editBottomSheetVisibility = MutableStateFlow(false)
    val editBottomSheetVisibility = _editBottomSheetVisibility.asStateFlow()
    private var _itemText = MutableStateFlow("")
    val itemText = _itemText.asStateFlow()
    private var _itemId = MutableStateFlow(0)
    val itemId = _itemId.asStateFlow()
    private var _itemTextError = MutableStateFlow("")
    val itemTextError = _itemTextError.asStateFlow()


    private var _originalItemsList = GetItems()

    private var _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    fun openEditBottomSheet(open: Boolean) {
        _editBottomSheetVisibility.value = open
    }

    fun setItemText(text: String) {
        _itemText.value = text
    }

    fun setItemId(id: Int) {
        _itemId.value = id
    }

    fun changeSearchText(text: String) {
        if (text.isEmpty()) {
            _itemList.value = _originalItemsList
        }

        _searchText.value = text

        if (_searchText.value.isNotEmpty()) {
            val output = _originalItemsList
                .asSequence()
                .map { it to FuzzySearch.ratio(it.name, _searchText.value) }
                .sortedByDescending { it.second }
                .filter { it.second != 0 }
                .take(5)
                .map { it.first }
                .toList()

            _itemList.value = ArrayList(output.distinct())
        }
    }

    private fun clearSearchText() {
        changeSearchText("")
    }

    fun getItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dorDamBDRepository.getItems()
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
                        _itemList.value = GetItems()
                        _error.value = it.errorMessage.toString()
                    }

                    is Resource.Success -> {
                        _isLoading.value = false
                        _hasError.value = false
                        _error.value = ""

                        clearSearchText()
                        _itemList.value = it.data!!
                        _originalItemsList = it.data
                    }
                }
            }
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