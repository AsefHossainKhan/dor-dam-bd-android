package com.asef.dordambdandroid.ui.screens

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object HomeScreen: Screen("home_screen")

    object PriceScreen: Screen("price_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach {
                val encodedString = URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                append("/$encodedString")
            }
        }
    }
}