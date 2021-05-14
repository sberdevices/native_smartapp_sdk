package ru.sberdevices.camera.factories.session

import android.hardware.camera2.CameraCaptureSession
import android.view.Surface
import ru.sberdevices.camera.utils.CameraExceptionHandler

internal class CameraSessionFactory(
    private val exceptionHandler: CameraExceptionHandler,
) {

    fun createSession(surfaces: List<Surface>, session: CameraCaptureSession): CameraSession {
        return CameraSessionWrapper(
            session,
            surfaces,
            exceptionHandler
        )
    }
}
