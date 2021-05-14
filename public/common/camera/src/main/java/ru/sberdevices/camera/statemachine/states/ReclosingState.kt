package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraState
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

/**
 * Camera is going to open soon and we want to close it right after it opened
 */
internal class ReclosingState(private val machine: CameraStateMachine) : CameraState() {
    private var closeAfterOpened = true

    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Start -> {
                closeAfterOpened = false
            }
            CameraAction.Stop -> {
                closeAfterOpened = true
            }
            CameraAction.Covered -> {
                machine.state = CoveredState(
                    machine,
                    camera = null,
                    openAfterUncover = false
                )
            }
            CameraAction.Uncovered -> {
                machine.illegalAction(action)
            }
            is CameraAction.Snapshot -> {
                /* ignore */
            }
            is CameraAction.Callback.Opened -> {
                machine.state = if (closeAfterOpened) {
                    ClosingState(
                        machine,
                        action.camera,
                        restart = false
                    )
                } else {
                    StartingSessionState(
                        machine,
                        action.camera
                    )
                }
            }
            CameraAction.Callback.Disconnected -> {
                machine.state = ClosedState(machine)
            }
            is CameraAction.Callback.Error -> {
                machine.state = ClosedState(machine)
            }
            is CameraAction.Callback.SessionConfigured -> machine.illegalAction(action)
            CameraAction.Callback.SessionFailed -> machine.illegalAction(action)
            CameraAction.Callback.PreviewStarted -> machine.illegalAction(action)
        }.exhaustive
    }
}
