package com.asef.dordambdandroid

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.asef.dordambdandroid.ui.screens.Screen
import com.asef.dordambdandroid.ui.screens.homescreen.HomeScreen
import com.asef.dordambdandroid.ui.screens.pricescreen.PriceScreen


@Composable()
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController)
        }

        composable(
            route = Screen.PriceScreen.route + "/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) {
            PriceScreen(navController, itemId = it.arguments?.getInt("itemId") ?: 0)
        }
    }
}