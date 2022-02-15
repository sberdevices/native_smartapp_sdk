package ru.sberdevices.camera.statemachine

internal abstract class CameraState {
    open fun onEnter() {}
    open fun onAction(action: CameraAction) {}
    open fun onLeave() {}
}
