package ru.sberdevices.camera.utils

import android.os.Handler
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import ru.sberdevices.camera.statemachine.ActionDispatcher
import ru.sberdevices.camera.statemachine.CameraAction

@MainThread
interface CameraCoveredListener {
    fun onCameraCovered()
    fun onCameraUncovered()
}

@MainThread
internal class CameraCoveredListenerImpl @AnyThread constructor(
    private val dispatcher: ActionDispatcher,
    private val cameraHandler: Handler,
) : CameraCoveredListener {

    override fun onCameraCovered() {
        cameraHandler.post {
            dispatcher.dispatch(CameraAction.Covered)
        }
    }

    override fun onCameraUncovered() {
        cameraHandler.post {
            dispatcher.dispatch(CameraAction.Uncovered)
        }
    }
}
