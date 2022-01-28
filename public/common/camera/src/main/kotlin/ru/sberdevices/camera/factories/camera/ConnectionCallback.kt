package ru.sberdevices.camera.factories.camera

import android.hardware.camera2.CameraDevice
import ru.sberdevices.camera.statemachine.ActionDispatcher
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.common.logger.Logger

internal class ConnectionCallback(
    private val dispatcher: ActionDispatcher,
    private val cameraFactory: CameraFactory,
) : CameraDevice.StateCallback() {

    private val logger = Logger.get("ConnectionCallback")

    private var camera: CameraDevice? = null

    override fun onOpened(camera: CameraDevice) {
        logger.debug { "onOpened" }
        val safeCamera = cameraFactory.createCamera(camera)
        val action = CameraAction.Callback.Opened(safeCamera)
        dispatcher.dispatch(action)
        this.camera = camera
    }

    override fun onDisconnected(camera: CameraDevice) {
        logger.debug { "onDisconnected. isPropagated = ${this.camera == camera}" }
        if (this.camera == camera) {
            dispatcher.dispatch(CameraAction.Callback.Disconnected)
            this.camera = null
        }
    }

    override fun onClosed(camera: CameraDevice) {
        logger.debug { "onClosed. isPropagated = ${this.camera == camera}" }
        if (this.camera == camera) {
            dispatcher.dispatch(CameraAction.Callback.Disconnected)
            this.camera = null
        }
    }

    override fun onError(camera: CameraDevice, error: Int) {
        logger.error { "onError $error. isPropagated = ${this.camera == camera}" }
        if (this.camera == camera) {
            dispatcher.dispatch(CameraAction.Callback.Error(error))
        } else if (this.camera == null) {
            dispatcher.dispatch(CameraAction.Callback.Disconnected)
        }
    }
}
