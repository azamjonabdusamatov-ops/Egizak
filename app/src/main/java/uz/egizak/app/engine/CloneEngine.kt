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

    /** Virtual user ids that currently exist. */
    fun userIds(): List<Int>

    /** Package names installed inside the given virtual user. */
    fun installedPackages(userId: Int): List<String>
}
