package uz.egizak.app

import android.app.Application
import uz.egizak.app.data.CloneRepository
import uz.egizak.app.engine.NotReadyEngine

class EgizakApp : Application() {
    // Stage 1: replace with BlackBoxCloneEngine and call BlackBoxCore.get().doAttachBaseContext(...) in attachBaseContext.
    val repository by lazy { CloneRepository(NotReadyEngine()) }
}
