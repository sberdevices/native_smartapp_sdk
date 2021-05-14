package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraState
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive
import java.lang.Exception

internal class OpeningState(
    private val machine: CameraStateMachine
) : CameraState() {

    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Start -> {
                /* ignore */
            }
            CameraAction.Stop -> {
                machine.state = ReclosingState(machine)
            }
            CameraAction.Covered -> {
                machine.state = CoveredState(
                    machine,
                    camera = null,
                    openAfterUncover = true
                )
            }
            CameraAction.Uncovered -> {
                machine.illegalAction(action)
            }
            is CameraAction.Snapshot -> {
                /* ignore */
            }
            is CameraAction.Callback.Opened -> {
                machine.state = StartingSessionState(
                    machine,
                    action.camera
                )
            }
            CameraAction.Callback.Disconnected,
            is CameraAction.Callback.Error,
            is CameraAction.Callback.SessionConfigured,
            CameraAction.Callback.SessionFailed,
            CameraAction.Callback.PreviewStarted -> {
                machine.state = OpeningState(machine)
            }
        }.exhaustive
    }

    override fun onEnter() {
        val exceptionHandler = machine.exceptionHandler
        try {
            machine.openCamera()
        } catch (e: SecurityException) {
            machine.state = ClosedState(machine)
            exceptionHandler.cameraException(e)
        } catch (e: Exception) {
            exceptionHandler.cameraException(e)
            try {
                Thread.sleep(1000)
                machine.state = OpeningState(machine)
            } catch (ignored: InterruptedException) {
                /* ignore */
            }
        }
    }
}
