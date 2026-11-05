package uz.egizak.app.engine

/**
 * Abstraction over the virtualization engine. Each clone of an app lives in its own
 * virtual user space identified by [userId], which is what makes unlimited clones possible.
 */
interface CloneEngine {
    fun install(packageName: String, userId: Int): Result<Unit>
    fun uninstall(packageName: String, userId: Int): Result<Unit>
    fun launch(packageName: String, userId: Int): Result<Unit>
    fun clearData(packageName: String, userId: Int): Result<Unit>
}

/** Placeholder until the NewBlackbox engine is wired in (stage 1). */
class NotReadyEngine : CloneEngine {
    private fun notReady(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Engine is not connected yet"))

    override fun install(packageName: String, userId: Int) = notReady()
    override fun uninstall(packageName: String, userId: Int) = notReady()
    override fun launch(packageName: String, userId: Int) = notReady()
    override fun clearData(packageName: String, userId: Int) = notReady()
}

/*
 * Stage 1 implementation sketch (NewBlackbox / BlackBoxCore API):
 *
 * class BlackBoxCloneEngine : CloneEngine {
 *     override fun install(packageName: String, userId: Int) = runCatching {
 *         val r = BlackBoxCore.get().installPackageAsUser(packageName, userId)
 *         check(r.success) { r.msg }
 *     }
 *     override fun uninstall(packageName: String, userId: Int) =
 *         runCatching { BlackBoxCore.get().uninstallPackageAsUser(packageName, userId) }
 *     override fun launch(packageName: String, userId: Int) =
 *         runCatching { BlackBoxCore.get().launchApk(packageName, userId) }
 *     override fun clearData(packageName: String, userId: Int) =
 *         runCatching { BlackBoxCore.get().clearPackage(packageName, userId) }
 * }
 */
