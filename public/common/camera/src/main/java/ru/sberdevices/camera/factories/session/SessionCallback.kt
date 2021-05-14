package ru.sberdevices.camera.factories.session

import android.hardware.camera2.CameraCaptureSession
import android.view.Surface
import ru.sberdevices.camera.statemachine.ActionDispatcher
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.common.logger.Logger

internal class SessionCallback(
    private val surfaces: List<Surface>,
    private val sessionFactory: CameraSessionFactory,
    private val dispatcher: ActionDispatcher
) : CameraCaptureSession.StateCallback() {

    private val logger = Logger.get("SessionCallback")

    override fun onConfigured(session: CameraCaptureSession) {
        logger.debug { "onConfigured" }
        val safeSession = sessionFactory.createSession(surfaces, session)
        dispatcher.dispatch(CameraAction.Callback.SessionConfigured(safeSession))
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        logger.error { "onConfigureFailed" }
        dispatcher.dispatch(CameraAction.Callback.SessionFailed)
    }
}
