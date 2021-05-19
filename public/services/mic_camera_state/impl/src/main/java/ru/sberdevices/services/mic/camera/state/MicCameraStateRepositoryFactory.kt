package ru.sberdevices.services.mic.camera.state

import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Factory for [MicCameraStateRepository].
 */
object MicCameraStateRepositoryFactory {
    @RequiresPermission("ru.sberdevices.permission.BIND_MIC_CAMERA_STATE_SERVICE")
    fun create(appContext: Context): MicCameraStateRepository = MicCameraStateRepositoryImpl(
        context = appContext.applicationContext,
        coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    )
}
