package uz.egizak.app.data

import android.content.Context

/** App-wide settings, stored on disk so every process (including virtual app
 *  processes the engine spawns) reads the same value at start-up. */
object Prefs {
    private const val FILE = "egizak_prefs"
    private const val KEY_HIDE_ROOT = "hide_root"

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun hideRoot(context: Context): Boolean = prefs(context).getBoolean(KEY_HIDE_ROOT, false)

    fun setHideRoot(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_HIDE_ROOT, value).apply()
    }
}
