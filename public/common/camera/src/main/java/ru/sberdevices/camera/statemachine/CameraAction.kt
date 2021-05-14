package ru.sberdevices.camera.statemachine

import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.session.CameraSession
import ru.sberdevices.camera.factories.snapshot.SnapshotCapturedCallback

internal sealed class CameraAction {
    object Start : CameraAction()
    object Stop : CameraAction()
    object Covered : CameraAction()
    object Uncovered : CameraAction()

    class Snapshot(val callback: SnapshotCapturedCallback) : CameraAction()

    sealed class Callback : CameraAction() {
        class Opened(val camera: Camera) : Callback()
        object Disconnected : Callback()
        class Error(error: Int) : Callback()
        class SessionConfigured(val session: CameraSession) : Callback()
        object SessionFailed : Callback()
        object PreviewStarted : Callback()
    }
}
