package uz.egizak.app.workprofile

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PersistableBundle

/**
 * Drives Android's built-in managed-profile ("work profile") feature. A work profile is a
 * real, OS-level profile, so apps running in it pass integrity checks that reject the
 * in-app virtualization engine. Limits: the OS allows one managed profile, so this gives at
 * most one extra instance per app, and some vendors (notably Huawei/Honor) disable it.
 */
object WorkProfileManager {

    /** Whether this device can create a work profile at all. */
    fun isSupported(context: Context): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching {
                dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
            }.getOrDefault(false)
        } else false
    }

    /** True once a work profile owned by Egizak exists. */
    fun hasProfile(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return runCatching { dpm.isProfileOwnerApp(context.packageName) }.getOrDefault(false)
    }

    /**
     * Launches the system flow that creates the work profile and, once ready, installs
     * [targetPackage] into it. The user confirms the profile creation in a system dialog.
     */
    fun provision(activity: Activity, targetPackage: String, requestCode: Int) {
        val extras = PersistableBundle().apply {
            putString(BankProfileAdmin.EXTRA_TARGET_PACKAGE, targetPackage)
        }
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                BankProfileAdmin.component(activity),
            )
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, extras)
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, false)
        }
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivityForResult(intent, requestCode)
        }
    }

    /** Installs another app into an already-created work profile (second clone of it). */
    fun installIntoExistingProfile(context: Context, targetPackage: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || !hasProfile(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return runCatching {
            dpm.installExistingPackage(BankProfileAdmin.component(context), targetPackage)
        }.getOrDefault(false)
    }
}
