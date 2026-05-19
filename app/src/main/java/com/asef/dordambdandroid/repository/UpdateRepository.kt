package com.asef.dordambdandroid.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import com.asef.dordambdandroid.BuildConfig
import com.asef.dordambdandroid.data.remote.models.update.GitHubRelease
import com.asef.dordambdandroid.util.Configuration
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val PREF_SHOULD_RUN_CLEANUP = "update_should_run_cleanup"

data class UpdateCheckResult(
    val isAvailable: Boolean,
    val latestVersion: String,
    val downloadUrl: String,
    val isForced: Boolean
)

@Singleton
class UpdateRepository @Inject constructor(
    private val client: OkHttpClient,
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    private val gson = Gson()

    fun checkForUpdate(): UpdateCheckResult {
        val request = Request.Builder()
            .url(Configuration.GITHUB_RELEASES_API_URL)
            .header("Accept", "application/vnd.github+json")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body.string()
        if (body.isEmpty()) return UpdateCheckResult(false, "", "", false)

        if (!response.isSuccessful) {
            Timber.w("GitHub releases API returned ${response.code}")
            return UpdateCheckResult(false, "", "", false)
        }

        val release = gson.fromJson(body, GitHubRelease::class.java)

        // Strip leading "v" from tag (e.g. "v1.2.3" → "1.2.3")
        val latestVersion = release.tagName.trimStart('v')
        val currentVersion = BuildConfig.VERSION_NAME

        if (!isNewer(latestVersion, currentVersion)) {
            return UpdateCheckResult(false, latestVersion, "", false)
        }

        val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
            ?: return UpdateCheckResult(false, latestVersion, "", false)

        val isForced = isUpdateForced(currentVersion, latestVersion)
        return UpdateCheckResult(true, latestVersion, apkAsset.browserDownloadUrl, isForced)
    }

    /** Returns true if [candidate] is strictly newer than [current] using semver ordering. */
    private fun isNewer(candidate: String, current: String): Boolean {
        val c = parseVersion(candidate) ?: return false
        val v = parseVersion(current) ?: return false
        if (c[0] != v[0]) return c[0] > v[0]
        if (c[1] != v[1]) return c[1] > v[1]
        return c[2] > v[2]
    }

    /**
     * Returns true if the update should be forced (non-dismissable).
     * Configured via [Configuration.FORCE_MAJOR_UPDATES], [Configuration.FORCE_MINOR_UPDATES],
     * and [Configuration.FORCE_PATCH_UPDATES].
     */
    private fun isUpdateForced(current: String, latest: String): Boolean {
        val c = parseVersion(current) ?: return false
        val l = parseVersion(latest) ?: return false
        return when {
            l[0] > c[0] -> Configuration.FORCE_MAJOR_UPDATES
            l[1] > c[1] -> Configuration.FORCE_MINOR_UPDATES
            else -> Configuration.FORCE_PATCH_UPDATES
        }
    }

    private fun parseVersion(version: String): List<Int>? {
        return try {
            version.split(".").map { it.toInt() }.takeIf { it.size == 3 }
        } catch (e: NumberFormatException) {
            Timber.e(e, "Failed to parse version string: $version")
            null
        }
    }

    /** Returns all ddbd-v*.apk files previously downloaded to the app's private downloads dir. */
    fun findDownloadedApks(): List<File> {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return emptyList()
        return dir.listFiles { file ->
            file.name.startsWith("ddbd-v") && file.name.endsWith(".apk")
        }?.toList() ?: emptyList()
    }

    fun deleteApks(files: List<File>) {
        files.forEach { file ->
            if (file.delete()) {
                Timber.d("Deleted APK: ${file.name}")
            } else {
                Timber.w("Failed to delete APK: ${file.name}")
            }
        }
    }

    /** Default: true — so the very first update cycle always runs a cleanup check. */
    fun shouldRunCleanup(): Boolean =
        sharedPreferences.getBoolean(PREF_SHOULD_RUN_CLEANUP, true)

    /** Call after the user responds to the cleanup prompt (confirm or skip). */
    fun setCleanupDone() {
        sharedPreferences.edit().putBoolean(PREF_SHOULD_RUN_CLEANUP, false).apply()
    }

    /** Call when the install intent is fired — resets for the next update cycle. */
    fun resetCleanupFlag() {
        sharedPreferences.edit().putBoolean(PREF_SHOULD_RUN_CLEANUP, true).apply()
    }
}
