package com.asef.dordambdandroid.ui.screens.homescreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import com.asef.dordambdandroid.repository.DorDamBDRepository
import com.asef.dordambdandroid.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
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
    private var _itemList = MutableStateFlow(GetItems())
    val itemList = _itemList.asStateFlow()

    fun getItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dorDamBDRepository.getItems()
            response.catch {
                Timber.e("Error $this")
            }
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _isLoading.value = true
                        }
                        is Resource.Error -> {
                            _isLoading.value = false
                            _hasError.value = true
                            _itemList.value = GetItems()
                            _error.value = resource.errorMessage.toString()
                        }
                        is Resource.Success -> {
                            _isLoading.value = false
                            _hasError.value = false
                            _error.value = ""

                            _itemList.value = resource.data!!
                        }
                    }

                }
        }
    }
}