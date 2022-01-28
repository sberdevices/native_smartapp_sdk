package ru.sberdevices.camera.statemachine.states

import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.session.CameraSession
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class PreviewStartedState(
    machine: CameraStateMachine,
    camera: Camera,
    session: CameraSession
) : SessionStartedState(machine, camera, session)
