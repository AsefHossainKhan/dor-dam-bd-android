package com.asef.dordambdandroid.ui.screens.homescreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.ui.components.AddFAB
import com.asef.dordambdandroid.ui.components.EditBottomSheet
import com.asef.dordambdandroid.ui.components.PullToRefreshLazyColumn
import com.asef.dordambdandroid.ui.screens.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController, modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val itemList by homeViewModel.itemList.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val searchText by homeViewModel.searchText.collectAsState()
    LaunchedEffect(key1 = Unit) {
        homeViewModel.getItems()
    }

    Scaffold(floatingActionButton = { AddItemFab(homeViewModel) }) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshLazyColumn(items = itemList, isRefreshing = isLoading, onRefresh = {
                homeViewModel.getItems()
            }, extraContent = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Items List")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = searchText,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        label = { Text(text = "Search Items") },
                        onValueChange = { homeViewModel.changeSearchText(it) })
                }
            }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .combinedClickable(true, onClick = {
                            navController.navigate(
                                Screen.PriceScreen.withArgs(
                                    item.id.toString(), item.name
                                )
                            )
                        }, onLongClick = {
                            homeViewModel.setItemText(item.name)
                            homeViewModel.setItemId(item.id)
                            homeViewModel.openEditBottomSheet(true)
                        }),
                    Arrangement.Center,
                    Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            EditItemBottomSheet(homeViewModel = homeViewModel)
        }
    }
}

@Composable
fun EditItemBottomSheet(
    homeViewModel: HomeViewModel
) {
    val modalSheetVisibility by homeViewModel.editBottomSheetVisibility.collectAsState()
    val focusManager = LocalFocusManager.current
    val itemName by homeViewModel.itemText.collectAsState()
    val itemId by homeViewModel.itemId.collectAsState()
    EditBottomSheet(modalSheetVisibility,
        openSheet = { homeViewModel.openEditBottomSheet(true) },
        closeSheet = { homeViewModel.openEditBottomSheet(false) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = itemName,
                    onValueChange = { homeViewModel.setItemText(it) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    label = {
                        Text(
                            text = "Edit Item Name"
                        )
                    })
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    homeViewModel.editItem(itemId, itemName)
                    homeViewModel.openEditBottomSheet(false)
                    focusManager.clearFocus()
                }) {
                    Text(text = "Update Item")
                }
            }
        }
    }
}

@Composable
fun AddItemFab(
    homeViewModel: HomeViewModel
) {
    var modalSheetVisibility by rememberSaveable {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current
    AddFAB(modalSheetVisibility,
        openSheet = { modalSheetVisibility = true },
        closeSheet = { modalSheetVisibility = false }) {
        var itemName by rememberSaveable {
            mutableStateOf("")
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = itemName,
                    onValueChange = { itemName = it },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    label = {
                        Text(
                            text = "Add Item"
                        )
                    })
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    homeViewModel.postItem(
                        CreateItem(name = itemName)
                    )
                    modalSheetVisibility = false
                    focusManager.clearFocus()
                }) {
                    Text(text = "Create Item")
                }
            }
        }
    }
}