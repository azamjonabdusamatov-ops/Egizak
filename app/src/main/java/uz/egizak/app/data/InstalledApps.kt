package uz.egizak.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

data class InstalledApp(val packageName: String, val label: String, val icon: Drawable)

fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    return pm.getInstalledApplications(0)
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 && it.packageName != context.packageName }
        .map { InstalledApp(it.packageName, pm.getApplicationLabel(it).toString(), pm.getApplicationIcon(it)) }
        .sortedBy { it.label.lowercase() }
}
