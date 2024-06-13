package com.asef.dordambdandroid.ui.screens.pricescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.asef.dordambdandroid.ui.components.AddFAB
import com.asef.dordambdandroid.ui.components.PullToRefreshLazyColumn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PriceScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    itemId: Int,
    itemName: String,
) {
    val priceViewModel: PriceViewModel = hiltViewModel()
    val priceList by priceViewModel.priceList.collectAsState()
    val isLoading by priceViewModel.isLoading.collectAsState()
    LaunchedEffect(Unit) {
        priceViewModel.getPrices(itemId)
    }
    Scaffold(floatingActionButton = {
        AddPriceFab(
            priceViewModel = priceViewModel,
            itemId = itemId
        )
    }) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {
            PullToRefreshLazyColumn(items = priceList, isRefreshing = isLoading, onRefresh = {
                priceViewModel.getPrices(itemId)
            }, extraContent = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = itemName)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        Arrangement.SpaceEvenly
                    ) {
                        Text(text = "Item Price")
                        Text(text = "Upload Date")
                    }
                }
            }) { item ->
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Text(text = item.price.toString() + " BDT")
                    val dateTimeFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date: Date? = dateTimeFormat.parse(item.createdAt)
                    // Extract day, month, year, hour, minute, and second
                    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
                    val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
                    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                    val hourFormat = SimpleDateFormat("hh", Locale.getDefault())
                    val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
                    val secondFormat = SimpleDateFormat("ss", Locale.getDefault())
                    val amPmFormat = SimpleDateFormat("a", Locale.getDefault())  // AM/PM
                    // Optional: You might want these to be in local time
                    dayFormat.timeZone = TimeZone.getDefault()
                    monthFormat.timeZone = TimeZone.getDefault()
                    yearFormat.timeZone = TimeZone.getDefault()
                    hourFormat.timeZone = TimeZone.getDefault()
                    minuteFormat.timeZone = TimeZone.getDefault()
                    secondFormat.timeZone = TimeZone.getDefault()
                    amPmFormat.timeZone = TimeZone.getDefault()
                    val day = date?.let { it1 -> dayFormat.format(it1) }
                    val month = date?.let { it1 -> monthFormat.format(it1) }
                    val year = date?.let { it1 -> yearFormat.format(it1) }
                    val hour = date?.let { it1 -> hourFormat.format(it1) }
                    val minute = date?.let { it1 -> minuteFormat.format(it1) }
                    val second = date?.let { it1 -> secondFormat.format(it1) }
                    val amPm = date?.let { it1 -> amPmFormat.format(it1) }

                    Text(text = "$hour:$minute $amPm - $day/$month/$year")
                }
            }
        }
    }
}

@Composable
fun AddPriceFab(
    priceViewModel: PriceViewModel,
    itemId: Int
) {
    var modalSheetVisibility by rememberSaveable {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current
    AddFAB(
        modalSheetVisibility,
        openSheet = { modalSheetVisibility = true },
        closeSheet = { modalSheetVisibility = false }
    ) {
        var price by rememberSaveable {
            mutableStateOf("")
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = price,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = { price = it },
                    label = {
                        Text(
                            text = "Add Price"
                        )
                    })
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    priceViewModel.addPrice(itemId = itemId, price = price.toFloat())
                    modalSheetVisibility = false
                    focusManager.clearFocus()
                }) {
                    Text(text = "Add Price")
                }
            }
        }
    }
}