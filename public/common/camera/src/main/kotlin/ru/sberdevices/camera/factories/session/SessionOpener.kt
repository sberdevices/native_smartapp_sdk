package ru.sberdevices.camera.factories.session

import android.os.Handler
import android.view.Surface
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.ActionDispatcher
import ru.sberdevices.common.logger.Logger

internal interface SessionOpener {
    fun openSession(camera: Camera, surfaces: List<Surface>): Boolean
}

internal class SessionOpenerImpl(
    private val sessionFactory: CameraSessionFactory,
    private val dispatcher: ActionDispatcher,
    private val cameraHandler: Handler,
) : SessionOpener {
    private val logger = Logger.get("SessionOpener")

    override fun openSession(camera: Camera, surfaces: List<Surface>): Boolean {
        logger.debug { "openSession" }
        return camera.createCaptureSession(
            surfaces,
            SessionCallback(
                surfaces,
                sessionFactory,
                dispatcher
            ),
            cameraHandler
        )
    }
}
