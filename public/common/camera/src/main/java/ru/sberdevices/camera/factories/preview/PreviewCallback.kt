package ru.sberdevices.camera.factories.preview

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import ru.sberdevices.camera.statemachine.ActionDispatcher
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.common.logger.Logger

internal class PreviewCallback(private val dispatcher: ActionDispatcher) : CameraCaptureSession.CaptureCallback() {
    private val logger = Logger.get("PreviewCallback")

    private var isFirstCapture = true

    override fun onCaptureStarted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        timestamp: Long,
        frameNumber: Long
    ) {
        if (isFirstCapture) {
            logger.debug { "onCaptureStarted" }
            dispatcher.dispatch(CameraAction.Callback.PreviewStarted)
            isFirstCapture = false
        }
    }
}
