package com.asef.dordambdandroid.ui.screens

sealed class Screen(val route: String) {
    object HomeScreen: Screen("home_screen")

    object PriceScreen: Screen("price_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach {
                append("/$it")
            }
        }
    }
}