package ru.sberdevices.camera.controller

import android.view.Surface
import androidx.annotation.AnyThread
import kotlinx.coroutines.flow.Flow
import ru.sberdevices.camera.factories.camera.CameraInfoProvider
import ru.sberdevices.camera.factories.snapshot.SnapshotCapturedCallback

@AnyThread
interface CameraController {
    val cameraInfoProvider: Flow<CameraInfoProvider>

    fun start(cameraId: String, surfaces: List<Surface>)
    fun snapshot(callback: SnapshotCapturedCallback)
    fun stop()
    fun release()
}
