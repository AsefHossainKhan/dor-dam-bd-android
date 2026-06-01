package com.asef.dordambdandroid.util

object Configuration {
//    const val BASE_URL = "http://192.168.66.132:3000/"
    const val BASE_URL = "https://dordambd.duckdns.org/"

    // Auto-update
    const val GITHUB_RELEASES_API_URL =
        "https://api.github.com/repos/AsefHossainKhan/dor-dam-bd-android/releases/latest"

    // Force update behaviour: major and minor bumps are forced, patch is optional by default.
    const val FORCE_MAJOR_UPDATES = true
    const val FORCE_MINOR_UPDATES = true
    const val FORCE_PATCH_UPDATES = false
}