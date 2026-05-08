package com.everlauncher.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.everlauncher.domain.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetectInstalledAppsUseCase(private val context: Context) {

    private val systemPackagePrefixes = listOf(
        "com.android.",
        "android.",
        "com.google.android.packageinstaller",
        "com.google.android.permissioncontroller",
        "com.google.android.ext.services",
        "com.google.android.ext.shared",
    )

    suspend fun getInstalledLaunchableApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }

        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }

        activities
            .filter { info -> !isSystemUtilityPackage(info.activityInfo.packageName) }
            .map { info ->
                val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                AppItem(
                    displayName = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    isSystemApp = isSystem,
                )
            }
            .sortedBy { it.displayName.lowercase() }
    }

    private fun isSystemUtilityPackage(packageName: String): Boolean =
        systemPackagePrefixes.any { packageName.startsWith(it) }
}
