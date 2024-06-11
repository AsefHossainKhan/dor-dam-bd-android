package com.asef.dordambdandroid

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asef.dordambdandroid.ui.screens.Screen
import com.asef.dordambdandroid.ui.screens.homescreen.HomeScreen


@Composable()
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController)
        }
    }
}