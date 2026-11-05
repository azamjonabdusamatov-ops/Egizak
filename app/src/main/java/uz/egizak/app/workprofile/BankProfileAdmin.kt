package uz.egizak.app.workprofile

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Profile owner of the work profile Egizak creates. A copy of Egizak runs inside the
 * new profile; this receiver is its admin. On provisioning it enables the profile and
 * installs the chosen app (e.g. a bank app) into the profile, where it runs as a real,
 * system-trusted second instance — which is what bank integrity checks accept.
 */
class BankProfileAdmin : DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, BankProfileAdmin::class.java)

        runCatching {
            dpm.setProfileName(admin, "Egizak")
            dpm.setProfileEnabled(admin)
        }.onFailure { Log.e(TAG, "enable profile", it) }

        // The app to clone was passed through the provisioning extras bundle.
        val target = intent.getParcelableExtra<android.os.PersistableBundle>(
            DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
        )?.getString(EXTRA_TARGET_PACKAGE)

        if (target != null) installIntoProfile(context, dpm, admin, target)
    }

    private fun installIntoProfile(
        context: Context,
        dpm: DevicePolicyManager,
        admin: ComponentName,
        packageName: String,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Log.w(TAG, "installExistingPackage needs API 28+")
            return
        }
        runCatching {
            val ok = dpm.installExistingPackage(admin, packageName)
            Log.d(TAG, "installExistingPackage($packageName) = $ok")
        }.onFailure { Log.e(TAG, "installExistingPackage", it) }
    }

    companion object {
        private const val TAG = "BankProfileAdmin"
        const val EXTRA_TARGET_PACKAGE = "uz.egizak.target_package"

        fun component(context: Context) = ComponentName(context, BankProfileAdmin::class.java)
    }
}
