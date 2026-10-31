package com.inspiredandroid.kai.updater

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.getWorkInfosForUniqueWorkFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * FOSS Android update controls rendered inside Settings > General.
 *
 * Discovery and download are separate from installation. The install button re-verifies the
 * APK immediately before handing it to Android's system package installer.
 */
@Composable
internal fun AndroidUpdateSettingsSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val workManager = remember(context) { WorkManager.getInstance(context.applicationContext) }
    val workInfos by workManager
        .getWorkInfosForUniqueWorkFlow(AndroidUpdateDownloadWorker.WORK_NAME)
        .collectAsState(initial = emptyList())

    var checking by remember { mutableStateOf(false) }
    var discoveryMessage by remember { mutableStateOf("Check GitHub for the latest stable release.") }
    var manifest by remember { mutableStateOf<AndroidUpdateManifest?>(null) }
    var downloadedApk by remember { mutableStateOf<File?>(null) }
    var installerMessage by remember { mutableStateOf<String?>(null) }

    var onPermissionReturned by remember { mutableStateOf<() -> Unit>({}) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        // Permission is not proof of installability: verify the APK again after returning.
        onPermissionReturned()
    }

    fun launchVerifiedInstall() {
        val currentManifest = manifest ?: return
        val apk = downloadedApk ?: return
        when (val result = AndroidUpdateInstaller(context).prepareInstall(currentManifest, apk)) {
            is AndroidUpdateInstaller.InstallPreparation.Ready -> {
                try {
                    context.startActivity(result.installIntent)
                    installerMessage = "Android's installer was opened. Confirm the update there to continue."
                } catch (_: ActivityNotFoundException) {
                    installerMessage = "Android could not open the package installer."
                }
            }
            is AndroidUpdateInstaller.InstallPreparation.PermissionRequired -> {
                installerMessage = "Allow installation from this source, then return here to verify the APK again."
                permissionLauncher.launch(result.settingsIntent)
            }
            is AndroidUpdateInstaller.InstallPreparation.Unsupported -> installerMessage = result.reason
            is AndroidUpdateInstaller.InstallPreparation.Invalid -> installerMessage = "APK verification failed: ${result.reason}"
        }
    }
    onPermissionReturned = { launchVerifiedInstall() }

    LaunchedEffect(workInfos, manifest) {
        val completed = workInfos.firstOrNull { it.state == WorkInfo.State.SUCCEEDED } ?: return@LaunchedEffect
        val path = completed.outputData.getString(AndroidUpdateDownloadWorker.KEY_FILE_PATH) ?: return@LaunchedEffect
        val currentManifest = manifest ?: return@LaunchedEffect
        val file = runCatching { File(path).canonicalFile }.getOrNull() ?: return@LaunchedEffect
        if (file.name == currentManifest.assetName && file.isFile && file.length() > 0L) {
            downloadedApk = file
            installerMessage = "Download complete. Verify and install when ready."
        }
    }

    val activeWork = workInfos.firstOrNull {
        it.state == WorkInfo.State.ENQUEUED ||
            it.state == WorkInfo.State.RUNNING ||
            it.state == WorkInfo.State.BLOCKED
    }
    val progress = activeWork?.progress
    val bytes = progress?.getLong(AndroidUpdateDownloadWorker.KEY_BYTES_DOWNLOADED, 0L) ?: 0L
    val total = progress?.getLong(AndroidUpdateDownloadWorker.KEY_TOTAL_BYTES, -1L) ?: -1L
    val isDownloading = activeWork != null

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("App updates", style = MaterialTheme.typography.titleMedium)
            Text(discoveryMessage, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = {
                    checking = true
                    discoveryMessage = "Checking the latest stable GitHub release…"
                    manifest = null
                    downloadedApk = null
                    installerMessage = null
                    scope.launch {
                        val result = runCatching {
                            AndroidUpdateDiscovery.check(context.installedVersionCode())
                        }
                        checking = false
                        result.fold(
                            onSuccess = { update ->
                                when (update) {
                                    AndroidUpdateDiscovery.Result.UpToDate -> {
                                        discoveryMessage = "This installation is up to date."
                                    }
                                    is AndroidUpdateDiscovery.Result.InvalidRelease -> {
                                        discoveryMessage = "Update metadata could not be validated: ${update.reason}"
                                    }
                                    is AndroidUpdateDiscovery.Result.UpdateAvailable -> {
                                        manifest = update.manifest
                                        discoveryMessage = "Version ${update.manifest.versionName} is available."
                                    }
                                }
                            },
                            onFailure = {
                                discoveryMessage = "Update check failed. Check your connection and try again."
                            },
                        )
                    }
                },
                enabled = !checking && !isDownloading,
            ) {
                Text(if (checking) "Checking…" else "Check for updates")
            }

            manifest?.let { available ->
                Text(
                    "Release ${available.versionName} (versionCode ${available.versionCode})",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (downloadedApk == null) {
                    OutlinedButton(
                        onClick = {
                            installerMessage = null
                            AndroidUpdateDownloadScheduler.enqueue(
                                context = context,
                                downloadUrl = available.downloadUrl,
                                sha256 = available.sha256,
                                assetName = available.assetName,
                            )
                        },
                        enabled = !isDownloading,
                    ) {
                        Text("Download update")
                    }
                } else {
                    Button(onClick = { launchVerifiedInstall() }) {
                        Text("Verify and install")
                    }
                }
            }

            if (isDownloading) {
                if (total > 0L) {
                    LinearProgressIndicator(
                        progress = { (bytes.toFloat() / total.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "${formatBytes(bytes)} of ${formatBytes(total)} downloaded",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Downloading update…", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = { AndroidUpdateDownloadScheduler.cancel(context) }) {
                    Text("Cancel download")
                }
            }

            workInfos.firstOrNull { it.state == WorkInfo.State.FAILED }?.let { failed ->
                val error = failed.outputData.getString(AndroidUpdateDownloadWorker.KEY_ERROR)
                Text(error ?: "Update download failed.", color = MaterialTheme.colorScheme.error)
            }
            installerMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

private fun Context.installedVersionCode(): Long {
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else {
        @Suppress("DEPRECATION")
        info.versionCode.toLong()
    }
}

private fun formatBytes(value: Long): String {
    if (value < 1024L) return "${value} B"
    val kib = value / 1024.0
    if (kib < 1024.0) return "${"%.1f".format(kib)} KB"
    return "${"%.1f".format(kib / 1024.0)} MB"
}

