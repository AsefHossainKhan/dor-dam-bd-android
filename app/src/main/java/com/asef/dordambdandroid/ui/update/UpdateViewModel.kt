package com.asef.dordambdandroid.ui.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asef.dordambdandroid.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data object UpToDate : UpdateState()
    data class UpdateAvailable(
        val latestVersion: String,
        val downloadUrl: String,
        val isForced: Boolean
    ) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class CleanupPrompt(val apkFiles: List<File>) : UpdateState()
    data object ReadyToInstall : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _installIntentEvent = MutableSharedFlow<Intent>()
    val installIntentEvent: SharedFlow<Intent> = _installIntentEvent.asSharedFlow()

    // Kept as a field so we can query progress and resolve the file URI on completion.
    private var activeDownloadId: Long = -1

    // The APK file destination — needed for FileProvider URI after download.
    private var pendingApkFile: File? = null

    fun checkForUpdates() {
        if (_updateState.value !is UpdateState.Idle && _updateState.value !is UpdateState.UpToDate &&
            _updateState.value !is UpdateState.Error
        ) return

        viewModelScope.launch(Dispatchers.IO) {
            _updateState.value = UpdateState.Checking
            runCatching { updateRepository.checkForUpdate() }
                .onSuccess { result ->
                    _updateState.value = if (result.isAvailable) {
                        UpdateState.UpdateAvailable(
                            result.latestVersion,
                            result.downloadUrl,
                            result.isForced
                        )
                    } else {
                        UpdateState.UpToDate
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Update check failed")
                    _updateState.value = UpdateState.Error(e.message ?: "Update check failed")
                }
        }
    }

    fun startDownload(latestVersion: String, downloadUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateState.value = UpdateState.Downloading(0)

            val fileName = "ddbd-v$latestVersion.apk"
            val destFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            // Remove any stale partial file from a previous attempt.
            if (destFile.exists()) destFile.delete()
            pendingApkFile = destFile

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Dor Dam BD Update v$latestVersion")
                .setDescription("Downloading update…")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(destFile))

            activeDownloadId = dm.enqueue(request)

            pollDownloadProgress(dm, activeDownloadId)
        }
    }

    private suspend fun pollDownloadProgress(dm: DownloadManager, downloadId: Long) {
        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = dm.query(query)

            if (cursor == null || !cursor.moveToFirst()) {
                cursor?.close()
                _updateState.value = UpdateState.Error("Download cancelled or missing")
                return
            }

            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val downloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            val status = cursor.getInt(statusIdx)
            val downloaded = cursor.getLong(downloadedIdx)
            val total = cursor.getLong(totalIdx)
            cursor.close()

            when (status) {
                DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING -> {
                    val progress = if (total > 0) ((downloaded * 100) / total).toInt() else 0
                    _updateState.value = UpdateState.Downloading(progress)
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    _updateState.value = UpdateState.Downloading(100)
                    onDownloadComplete()
                    return
                }
                DownloadManager.STATUS_FAILED -> {
                    val reason = getFailureReason(dm, downloadId)
                    _updateState.value = UpdateState.Error("Download failed: $reason")
                    return
                }
                DownloadManager.STATUS_PAUSED -> {
                    // Network temporarily unavailable — keep waiting.
                }
            }
            delay(500)
        }
    }

    private fun getFailureReason(dm: DownloadManager, downloadId: Long): String {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query) ?: return "Unknown"
        if (!cursor.moveToFirst()) { cursor.close(); return "Unknown" }
        val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(reasonIdx)
        cursor.close()
        return reason.toString()
    }

    private fun onDownloadComplete() {
        if (updateRepository.shouldRunCleanup()) {
            val apks = updateRepository.findDownloadedApks()
            if (apks.isNotEmpty()) {
                _updateState.value = UpdateState.CleanupPrompt(apks)
                return
            }
            // No APKs found — silently mark cleanup as done.
            updateRepository.setCleanupDone()
        }
        _updateState.value = UpdateState.ReadyToInstall
    }

    fun onCleanupResponse(confirmed: Boolean, files: List<File>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (confirmed) updateRepository.deleteApks(files)
            updateRepository.setCleanupDone()
            _updateState.value = UpdateState.ReadyToInstall
        }
    }

    fun triggerInstall() {
        viewModelScope.launch {
            val apkFile = pendingApkFile ?: run {
                _updateState.value = UpdateState.Error("APK file not found")
                return@launch
            }
            if (!apkFile.exists()) {
                _updateState.value = UpdateState.Error("APK file missing from storage")
                return@launch
            }

            val uri = withContext(Dispatchers.IO) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Reset cleanup flag now so the next update cycle re-runs the check.
            updateRepository.resetCleanupFlag()

            _installIntentEvent.emit(intent)
        }
    }

    fun dismissUpdate() {
        // Only valid for non-forced (patch) updates — the UI will not show the dismiss
        // button for forced updates, but guard here as well.
        _updateState.value = UpdateState.Idle
    }
}
