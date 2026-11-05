package uz.egizak.app.engine

import top.niunaijun.blackbox.BlackBoxCore

/** [CloneEngine] backed by the NewBlackbox virtualization engine. */
class BlackBoxCloneEngine : CloneEngine {
    private val core get() = BlackBoxCore.get()

    override fun install(packageName: String, userId: Int) = runCatching {
        core.createUser(userId) // no-op if the virtual user already exists
        val result = core.installPackageAsUser(packageName, userId)
        check(result.success) { result.msg ?: "Install failed" }
    }

    override fun uninstall(packageName: String, userId: Int) = runCatching {
        core.uninstallPackageAsUser(packageName, userId)
        // Drop the virtual user once it holds nothing, so its id can be reused.
        if (core.getInstalledApplications(0, userId).isEmpty()) core.deleteUser(userId)
    }

    override fun launch(packageName: String, userId: Int) = runCatching {
        check(core.launchApk(packageName, userId)) { "Could not launch $packageName" }
    }

    override fun clearData(packageName: String, userId: Int) = runCatching {
        core.clearPackage(packageName, userId)
    }

    override fun userIds(): List<Int> = core.users.map { it.id }.sorted()

    override fun installedPackages(userId: Int): List<String> =
        core.getInstalledApplications(0, userId).map { it.packageName }
}
