package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraState
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

/**
 * Camera is going to close soon and we want to open it right after disconnect
 */
internal class ReopeningState(
    private val machine: CameraStateMachine,
    private val camera: Camera
) : CameraState() {
    private var openAfterClose = true

    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Start -> {
                openAfterClose = true
            }
            CameraAction.Stop -> {
                openAfterClose = false
            }
            CameraAction.Covered -> {
                machine.state = CoveredState(
                    machine,
                    camera,
                    openAfterClose
                )
            }
            CameraAction.Uncovered -> {
                machine.illegalAction(action)
            }
            is CameraAction.Snapshot -> {
                /* ignore */
            }
            is CameraAction.Callback.Opened -> {
                machine.state = StartingSessionState(machine, action.camera)
            }
            CameraAction.Callback.Disconnected -> {
                machine.state = if (openAfterClose) {
                    OpeningState(machine)
                } else {
                    ClosedState(machine)
                }
            }
            is CameraAction.Callback.Error -> {
                /* ignore */
            }
            is CameraAction.Callback.SessionConfigured -> machine.illegalAction(action)
            CameraAction.Callback.SessionFailed -> machine.illegalAction(action)
            CameraAction.Callback.PreviewStarted -> machine.illegalAction(action)
        }.exhaustive
    }

    override fun onEnter() {
        if (!camera.close()) {
            machine.state = OpeningState(machine)
        }
    }
}
