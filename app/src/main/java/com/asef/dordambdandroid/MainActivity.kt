package com.asef.dordambdandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asef.dordambdandroid.ui.theme.DorDamBDAndroidTheme
import com.asef.dordambdandroid.ui.update.UpdateDialog
import com.asef.dordambdandroid.ui.update.UpdateState
import com.asef.dordambdandroid.ui.update.UpdateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val updateViewModel: UpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DorDamBDAndroidTheme {
                val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()

                // Kick off the update check once on launch.
                LaunchedEffect(Unit) {
                    updateViewModel.checkForUpdates()
                }

                // Observe install intent events and delegate to the OS package installer.
                LaunchedEffect(Unit) {
                    updateViewModel.installIntentEvent.collect { intent ->
                        startActivity(intent)
                    }
                }

                // Block back-press when a forced update is in progress so the user
                // cannot bypass the mandatory update by navigating away.
                val isForcedInProgress = when (val s = updateState) {
                    is UpdateState.UpdateAvailable -> s.isForced
                    is UpdateState.Downloading -> true
                    is UpdateState.CleanupPrompt -> true
                    is UpdateState.ReadyToInstall -> true
                    else -> false
                }
                BackHandler(enabled = isForcedInProgress) { finish() }

                Navigation()

                UpdateDialog(
                    state = updateState,
                    onUpdateNow = { version, url ->
                        updateViewModel.startDownload(version, url)
                    },
                    onDismiss = { updateViewModel.dismissUpdate() },
                    onCleanupDelete = {
                        val s = updateState
                        if (s is UpdateState.CleanupPrompt) {
                            updateViewModel.onCleanupResponse(confirmed = true, files = s.apkFiles)
                        }
                    },
                    onCleanupSkip = {
                        val s = updateState
                        if (s is UpdateState.CleanupPrompt) {
                            updateViewModel.onCleanupResponse(confirmed = false, files = s.apkFiles)
                        }
                    },
                    onInstallNow = { updateViewModel.triggerInstall() }
                )
            }
        }
    }
}