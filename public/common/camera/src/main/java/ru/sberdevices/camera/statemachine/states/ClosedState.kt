package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraState
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

internal class ClosedState(
    private val machine: CameraStateMachine
) : CameraState() {
    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Start -> {
                machine.state = OpeningState(machine)
            }
            CameraAction.Stop -> {
                /* ignore */
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
            is CameraAction.Callback.Opened -> {
                machine.illegalAction(action)
                machine.state = ClosingState(
                    machine,
                    action.camera,
                    restart = false
                )
            }
            is CameraAction.Snapshot -> {
                /* ignore */
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
}
