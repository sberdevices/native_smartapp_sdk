package ru.sberdevices.camera.statemachine.states

import androidx.annotation.VisibleForTesting
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraState
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

internal class ClosingState(
    private val machine: CameraStateMachine,
    private val camera: Camera,
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var restart: Boolean
) : CameraState() {

    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Start -> {
                machine.state =
                    ReopeningState(machine, camera)
            }
            CameraAction.Stop -> {
                restart = false
            }
            CameraAction.Covered -> {
                machine.state = CoveredState(
                    machine,
                    camera,
                    openAfterUncover = restart
                )
            }
            CameraAction.Uncovered -> {
                machine.illegalAction(action)
            }
            is CameraAction.Snapshot -> {
                /* ignore */
            }
            is CameraAction.Callback.Opened -> {
                machine.state =
                    ClosingState(machine, camera, restart)
            }
            CameraAction.Callback.Disconnected -> {
                machine.state = if (restart) OpeningState(
                    machine
                ) else ClosedState(machine)
            }
            is CameraAction.Callback.Error -> {
                machine.state = if (restart) ReopeningState(
                    machine,
                    camera
                ) else ClosedState(machine)
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
        if (!camera.close()) {
            machine.state = if (restart) OpeningState(
                machine
            ) else ClosedState(machine)
        }
    }
}
