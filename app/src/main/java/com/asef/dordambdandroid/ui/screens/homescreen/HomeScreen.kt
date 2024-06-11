package com.asef.dordambdandroid.ui.screens.homescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.asef.dordambdandroid.data.remote.models.items.getitems.GetItems
import timber.log.Timber

@Composable
fun HomeScreen(
    navController: NavController
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
//    val itemList: MutableState<GetItems> = rememberSaveable {
//        mutableStateOf(GetItems())
//    }
    val itemList by homeViewModel.itemList.collectAsState()
    Timber.e(itemList.toString())
    LaunchedEffect(key1 = Unit){
        homeViewModel.getItems()
    }
    Box {
        Text(text = "Hello World")
    }
}