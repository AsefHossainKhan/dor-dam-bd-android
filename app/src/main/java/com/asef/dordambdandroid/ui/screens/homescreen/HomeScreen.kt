package com.asef.dordambdandroid.ui.screens.homescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.asef.dordambdandroid.ui.screens.Screen
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
//    val itemList: MutableState<GetItems> = rememberSaveable {
//        mutableStateOf(GetItems())
//    }
    val itemList by homeViewModel.itemList.collectAsState()
    Timber.e(itemList.toString())
    LaunchedEffect(key1 = Unit) {
        homeViewModel.getItems()
    }
    Scaffold {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LazyColumn() {
                items(itemList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        Arrangement.Center,
                    ) {
                        Button(
                            onClick = {
                                Timber.e("${item.name} --- ${item.id}")
                                navController.navigate(Screen.PriceScreen.withArgs(item.id.toString()))
                            },
                        ) {
                            Text(text = item.name)
                        }
                    }
                }
            }
        }
    }
}