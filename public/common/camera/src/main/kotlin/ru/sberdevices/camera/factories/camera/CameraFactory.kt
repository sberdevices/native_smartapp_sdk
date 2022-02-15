package ru.sberdevices.camera.factories.camera

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.view.WindowManager
import ru.sberdevices.camera.utils.CameraExceptionHandler

internal class CameraFactory(
    private val cameraManager: CameraManager,
    private val windowManager: WindowManager,
    private val exceptionHandler: CameraExceptionHandler
) {

    fun createCamera(cameraDevice: CameraDevice): Camera {
        val cameraInfoProvider = CameraInfoProviderImpl(
            cameraDevice.id,
            cameraManager,
            windowManager.defaultDisplay.rotation
        )
        return CameraWrapper(
            cameraInfoProvider,
            exceptionHandler,
            cameraDevice
        )
    }
}
