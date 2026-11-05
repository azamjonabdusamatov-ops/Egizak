package uz.egizak.app.data

import uz.egizak.app.engine.CloneEngine

data class Clone(
    val packageName: String,
    val userId: Int,
    val label: String,
)

/**
 * Keeps track of clones and allocates a fresh virtual userId for each new one,
 * so the same app can be cloned any number of times.
 * In-memory for the skeleton; moves to Room in stage 2.
 */
class CloneRepository(private val engine: CloneEngine) {
    private val clones = mutableListOf<Clone>()

    fun all(): List<Clone> = clones.toList()

    fun clonesOf(packageName: String) = clones.filter { it.packageName == packageName }

    private fun nextFreeUserId(packageName: String): Int {
        val used = clonesOf(packageName).map { it.userId }.toSet()
        return generateSequence(0) { it + 1 }.first { it !in used }
    }

    fun addClone(packageName: String, appLabel: String): Result<Clone> {
        val userId = nextFreeUserId(packageName)
        return engine.install(packageName, userId).map {
            Clone(packageName, userId, "$appLabel ${userId + 1}").also { clones += it }
        }
    }

    fun removeClone(clone: Clone): Result<Unit> =
        engine.uninstall(clone.packageName, clone.userId).onSuccess { clones -= clone }

    fun launch(clone: Clone) = engine.launch(clone.packageName, clone.userId)
}
