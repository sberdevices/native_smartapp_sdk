package ru.sberdevices.camera.statemachine.states

import androidx.annotation.CallSuper
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraState
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

internal abstract class OpenedState(
    private val machine: CameraStateMachine,
    private val camera: Camera
) : CameraState() {

    @CallSuper
    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Start -> {
                /* ignore */
            }
            CameraAction.Stop -> {
                machine.state = ClosingState(
                    machine,
                    camera,
                    restart = false
                )
            }
            CameraAction.Covered -> {
                machine.state = CoveredState(
                    machine,
                    camera,
                    openAfterUncover = true
                )
            }
            CameraAction.Uncovered -> {
                machine.illegalAction(action)
            }
            is CameraAction.Snapshot -> {
                machine.state = SnapshotState(machine, camera, action.callback)
            }
            is CameraAction.Callback.Opened -> {
                machine.illegalAction(action)
                machine.state = ClosingState(machine, camera, restart = true)
            }
            CameraAction.Callback.Disconnected -> {
                machine.state = ClosedState(machine)
            }
            is CameraAction.Callback.Error -> {
                machine.state = ReopeningState(machine, camera)
            }
            is CameraAction.Callback.SessionConfigured -> machine.illegalAction(action)
            CameraAction.Callback.SessionFailed -> machine.illegalAction(action)
            CameraAction.Callback.PreviewStarted -> machine.illegalAction(action)
        }.exhaustive
    }
}
