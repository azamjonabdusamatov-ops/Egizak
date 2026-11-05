package uz.egizak.app.data

import android.content.pm.PackageManager
import uz.egizak.app.engine.CloneEngine

data class Clone(
    val packageName: String,
    val userId: Int,
    val label: String,
)

/**
 * Clones are read back from the engine, which persists them itself.
 * Clone N of an app lives in the lowest virtual user that does not have that app yet,
 * so the same app can be cloned any number of times.
 */
class CloneRepository(
    private val engine: CloneEngine,
    private val hostPm: PackageManager,
) {
    private fun labelOf(packageName: String): String = runCatching {
        hostPm.getApplicationLabel(hostPm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)

    fun all(): List<Clone> =
        engine.userIds().flatMap { uid ->
            engine.installedPackages(uid).map { pkg -> Clone(pkg, uid, "${labelOf(pkg)} ${uid + 1}") }
        }.sortedWith(compareBy({ it.label.lowercase() }, { it.userId }))

    private fun nextFreeUserId(packageName: String): Int {
        val used = engine.userIds().filter { packageName in engine.installedPackages(it) }.toSet()
        return generateSequence(0) { it + 1 }.first { it !in used }
    }

    fun addClone(packageName: String): Result<Clone> {
        val userId = nextFreeUserId(packageName)
        return engine.install(packageName, userId).map {
            Clone(packageName, userId, "${labelOf(packageName)} ${userId + 1}")
        }
    }

    fun removeClone(clone: Clone): Result<Unit> = engine.uninstall(clone.packageName, clone.userId)

    fun launch(clone: Clone) = engine.launch(clone.packageName, clone.userId)

    fun clearData(clone: Clone) = engine.clearData(clone.packageName, clone.userId)
}
