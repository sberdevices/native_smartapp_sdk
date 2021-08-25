package ru.sberdevices.services.mic.camera.state.aidl.wrappers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.mic.camera.state.MicCameraStateRepository

/**
 * Реализация обертки [OnMicCameraStateChangedListenerWrapper].
 */
internal class OnMicCameraStateChangedListenerWrapperImpl : OnMicCameraStateChangedListenerWrapper() {
    private val logger by Logger.lazy<OnMicCameraStateChangedListenerWrapperImpl>()

    override val micStateFlow: StateFlow<MicCameraStateRepository.State>
        get() = mutableMicStateFlow.asStateFlow()
    override val cameraStateFlow: StateFlow<MicCameraStateRepository.State>
        get() = mutableCameraStateFlow.asStateFlow()
    override val isCameraCoveredStateFlow: StateFlow<Boolean>
        get() = mutableIsCameraCoveredStateFlow.asStateFlow()

    private val mutableMicStateFlow = MutableStateFlow(MicCameraStateRepository.State.UNKNOWN)
    private val mutableCameraStateFlow = MutableStateFlow(MicCameraStateRepository.State.UNKNOWN)
    private val mutableIsCameraCoveredStateFlow = MutableStateFlow(false)

    override fun onCameraMicStateChanged(isMicDisabled: Boolean, isCameraDisabled: Boolean, isCameraCovered: Boolean) {
        val result = mutableMicStateFlow.tryEmit(
            if (isMicDisabled) MicCameraStateRepository.State.DISABLED else MicCameraStateRepository.State.ENABLED
        ) and mutableCameraStateFlow.tryEmit(
            if (isCameraDisabled) MicCameraStateRepository.State.DISABLED else MicCameraStateRepository.State.ENABLED
        ) and mutableIsCameraCoveredStateFlow.tryEmit(isCameraCovered)

        logger.debug { "onCameraMicStateChanged(), added to flow: $result" }
    }
}
