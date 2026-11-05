package uz.egizak.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class InstalledApp(val packageName: String, val label: String, val icon: ImageBitmap)

/** User-facing apps that can be cloned: anything with a launcher entry, except ourselves. */
fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    return pm.getInstalledApplications(0)
        .asSequence()
        .filter { it.packageName != context.packageName }
        .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
        .map { InstalledApp(it.packageName, pm.getApplicationLabel(it).toString(), pm.getApplicationIcon(it).toImageBitmap()) }
        .sortedBy { it.label.lowercase() }
        .toList()
}

/** Icon of an app installed on the host, by package name. Null if it's gone. */
fun appIcon(context: Context, packageName: String): ImageBitmap? = runCatching {
    context.packageManager.getApplicationIcon(packageName).toImageBitmap()
}.getOrNull()

fun appLabel(context: Context, packageName: String): String = runCatching {
    val pm = context.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
}.getOrDefault(packageName)

private fun Drawable.toImageBitmap(): ImageBitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap.asImageBitmap()
    val w = intrinsicWidth.takeIf { it > 0 } ?: 96
    val h = intrinsicHeight.takeIf { it > 0 } ?: 96
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp.asImageBitmap()
}
