package ru.sberdevices.services.mic.camera.state.aidl.wrappers

import kotlinx.coroutines.flow.StateFlow
import ru.sberdevices.services.mic.camera.state.MicCameraStateRepository
import ru.sberdevices.services.mic.camera.state.aidl.IOnMicCameraStateChangedListener

/**
 * Обертка над [IOnMicCameraStateChangedListener.Stub] для удобства тестирования.
 * Данные из [onCameraMicStateChanged] передаются во [flow].
 */
internal abstract class OnMicCameraStateChangedListenerWrapper : IOnMicCameraStateChangedListener.Stub() {
    abstract val micStateFlow: StateFlow<MicCameraStateRepository.State>
    abstract val cameraStateFlow: StateFlow<MicCameraStateRepository.State>
    abstract val isCameraCoveredStateFlow: StateFlow<Boolean>
}
