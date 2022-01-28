package ru.sberdevices.camera.statemachine.states

import android.hardware.camera2.CameraDevice
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.session.CameraSession
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.exhaustive

internal class StartingPreviewState(
    private val machine: CameraStateMachine,
    private val camera: Camera,
    session: CameraSession,
) : SessionStartedState(machine, camera, session) {

    override fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.Callback.PreviewStarted -> {
                machine.state = PreviewStartedState(machine, camera, session)
            }
            else -> super.onAction(action)
        }.exhaustive
    }

    override fun onEnter() {
        val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        if (request != null) {
            for (surface in session.surfaces) {
                request.addTarget(surface)
            }
            if (!session.setRepeatingBurst(request.build(), machine.createPreviewCallback(), machine.cameraHandler)) {
                machine.state = ClosingState(
                    machine,
                    camera,
                    restart = true
                )
            }
        } else {
            machine.state = ClosingState(
                machine,
                camera,
                restart = true
            )
        }
    }
}
