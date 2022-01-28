package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraState
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive
import ru.sberdevices.common.logger.Logger

internal class CoveredState(
    private val machine: CameraStateMachine,
    private val camera: Camera?,
    private var openAfterUncover: Boolean,
) : CameraState() {

    private val logger = Logger.get("CoveredState")

    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Start -> {
                openAfterUncover = true
            }
            CameraAction.Stop -> {
                openAfterUncover = false
            }
            CameraAction.Covered -> {
                machine.illegalAction(action)
            }
            CameraAction.Uncovered -> {
                machine.state = if (openAfterUncover) OpeningState(
                    machine
                ) else ClosedState(machine)
            }
            is CameraAction.Snapshot -> {
                /* ignore */
            }
            is CameraAction.Callback.Opened -> {
                if (!action.camera.close()) {
                    logger.warn { "wasn't able to close camera" }
                }
                Unit
            }
            CameraAction.Callback.Disconnected -> {
                /* ignore */
            }
            is CameraAction.Callback.Error -> {
                /* ignore */
            }
            is CameraAction.Callback.SessionConfigured -> {
                /* ignore */
            }
            CameraAction.Callback.SessionFailed -> {
                /* ignore */
            }
            CameraAction.Callback.PreviewStarted -> {
                /* ignore */
            }
        }.exhaustive
    }

    override fun onEnter() {
        if (camera != null && !camera.close()) {
            logger.warn { "wasn't able to close camera" }
        }
    }
}
