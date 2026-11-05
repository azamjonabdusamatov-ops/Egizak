package uz.egizak.app

import android.app.Application
import android.content.Context
import android.util.Log
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.configuration.ClientConfiguration
import uz.egizak.app.data.CloneRepository
import uz.egizak.app.engine.BlackBoxCloneEngine
import java.io.File

class EgizakApp : Application() {

    val repository by lazy { CloneRepository(BlackBoxCloneEngine(), packageManager) }

    // Same start-up sequence as the engine's reference app. This runs in every process,
    // including the virtual app processes the engine spawns.
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        val core = BlackBoxCore.get()
        runCatching { core.closeCodeInit() }.onFailure { Log.e(TAG, "closeCodeInit", it) }
        runCatching { core.onBeforeMainApplicationAttach(this, base) }.onFailure { Log.e(TAG, "beforeAttach", it) }
        runCatching {
            core.doAttachBaseContext(base, object : ClientConfiguration() {
                override fun getHostPackageName(): String = base.packageName
                override fun requestInstallPackage(file: File?, userId: Int): Boolean = false
                // The engine uploads full logcat to a third-party server on crashes by default.
                // Returning null turns that off: nothing leaves the phone.
                override fun getLogSenderChatId(): String? = null
            })
        }.onFailure { Log.e(TAG, "doAttachBaseContext", it) }
        runCatching { core.onAfterMainApplicationAttach(this, base) }.onFailure { Log.e(TAG, "afterAttach", it) }
    }

    override fun onCreate() {
        super.onCreate()
        runCatching { BlackBoxCore.get().doCreate() }.onFailure { Log.e(TAG, "doCreate", it) }
    }

    private companion object { const val TAG = "Egizak" }
}
