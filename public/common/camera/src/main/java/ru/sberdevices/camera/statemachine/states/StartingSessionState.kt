package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

internal class StartingSessionState(
    private val machine: CameraStateMachine,
    private val camera: Camera
) : OpenedState(machine, camera) {

    override fun onAction(action: CameraAction) {
        when (action) {
            is CameraAction.Callback.SessionConfigured -> {
                machine.state = StartingPreviewState(
                    machine,
                    camera,
                    action.session
                )
            }
            CameraAction.Callback.SessionFailed -> {
                machine.state =
                    ReopeningState(machine, camera)
            }
            CameraAction.Callback.PreviewStarted -> machine.illegalAction(action)
            else -> super.onAction(action)
        }.exhaustive
    }

    override fun onEnter() {
        val snapshotRequest = machine.snapshotRequest
        if (snapshotRequest != null) {
            machine.state = SnapshotState(machine, camera, snapshotRequest.capturedCallback)
        } else if (!machine.openSession(camera, machine.surfaces)) {
            machine.state = ReopeningState(machine, camera)
        }
    }
}
