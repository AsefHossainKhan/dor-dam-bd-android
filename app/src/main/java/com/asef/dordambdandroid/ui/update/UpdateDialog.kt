package com.asef.dordambdandroid.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asef.dordambdandroid.BuildConfig

@Composable
fun UpdateDialog(
    state: UpdateState,
    onUpdateNow: (latestVersion: String, downloadUrl: String) -> Unit,
    onDismiss: () -> Unit,
    onCleanupDelete: () -> Unit,
    onCleanupSkip: () -> Unit,
    onInstallNow: () -> Unit
) {
    when (state) {
        is UpdateState.UpdateAvailable -> {
            AlertDialog(
                onDismissRequest = { if (!state.isForced) onDismiss() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = { Text("Update Available") },
                text = {
                    Column {
                        Text(
                            "A new version of Dor Dam BD is available.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Current", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    BuildConfig.VERSION_NAME,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                "→",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Latest", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    state.latestVersion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (state.isForced) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "This update is required to continue using the app.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { onUpdateNow(state.latestVersion, state.downloadUrl) }) {
                        Text("Update Now")
                    }
                },
                dismissButton = {
                    if (!state.isForced) {
                        OutlinedButton(onClick = onDismiss) { Text("Later") }
                    }
                }
            )
        }

        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Downloading Update") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.progress > 0) {
                            LinearProgressIndicator(
                                progress = { state.progress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${state.progress}%",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Starting download…",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Please wait, do not close the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateState.CleanupPrompt -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Clean Up Old APKs?") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "The following update file(s) from previous downloads were found " +
                                    "and can be removed:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        state.apkFiles.forEach { file ->
                            Text(
                                "• ${file.name}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = onCleanupDelete) { Text("Delete") }
                },
                dismissButton = {
                    OutlinedButton(onClick = onCleanupSkip) { Text("Skip") }
                }
            )
        }

        is UpdateState.ReadyToInstall -> {
            AlertDialog(
                onDismissRequest = {},
                icon = {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = { Text("Ready to Install") },
                text = {
                    Text(
                        "The update has been downloaded. Tap Install to apply it now.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(onClick = onInstallNow) {
                        Text("Install Now")
                    }
                }
            )
        }

        else -> { /* Idle / Checking / UpToDate / Error — no dialog */ }
    }
}
