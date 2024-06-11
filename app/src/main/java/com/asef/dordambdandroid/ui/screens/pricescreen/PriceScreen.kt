package com.asef.dordambdandroid.ui.screens.pricescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun PriceScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    itemId: Int,
    ) {
    Scaffold {
        Box(modifier= modifier.padding(it)) {
            Text(text = itemId.toString())
        }
    }
}