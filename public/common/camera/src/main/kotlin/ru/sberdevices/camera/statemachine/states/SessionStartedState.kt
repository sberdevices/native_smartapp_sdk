package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.session.CameraSession
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

internal open class SessionStartedState(
    private val machine: CameraStateMachine,
    private val camera: Camera,
    protected val session: CameraSession
) : OpenedState(machine, camera) {

    override fun onAction(action: CameraAction) {
        when (action) {
            is CameraAction.Callback.SessionConfigured -> {
                session.close()
                machine.state = StartingSessionState(machine, camera)
            }
            CameraAction.Callback.SessionFailed -> {
                machine.state = ClosingState(
                    machine,
                    camera,
                    restart = true
                )
            }
            CameraAction.Callback.PreviewStarted -> machine.illegalAction(action)
            else -> super.onAction(action)
        }.exhaustive
    }
}
