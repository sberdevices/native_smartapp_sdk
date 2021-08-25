package ru.sberdevices.services.mic.camera.state

import androidx.annotation.AnyThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.sberdevices.common.binderhelper.BinderHelperFactory
import ru.sberdevices.common.binderhelper.entities.BinderState
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.mic.camera.state.aidl.IMicCameraStateService
import ru.sberdevices.services.mic.camera.state.aidl.wrappers.OnMicCameraStateChangedListenerWrapper

/**
 * Имплементация [MicCameraStateRepository].
 */
internal class MicCameraStateRepositoryImpl(
    helperFactory: BinderHelperFactory<IMicCameraStateService>,
    private val onMicCameraStateChangedListenerWrapper: OnMicCameraStateChangedListenerWrapper
) : MicCameraStateRepository {

    private val logger by lazy { Logger.get("MicCameraStateRepositoryImpl") }

    private val helper = helperFactory.createCached()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val micState: Flow<MicCameraStateRepository.State> = onMicCameraStateChangedListenerWrapper.micStateFlow
        .onEach { logger.debug { "MicState changed to $it" } }
        .flowOn(Dispatchers.Default)

    override val cameraState: Flow<MicCameraStateRepository.State> =
        onMicCameraStateChangedListenerWrapper.cameraStateFlow
            .onEach { logger.debug { "CameraState changed to $it" } }
            .flowOn(Dispatchers.Default)

    override val isCameraCovered: Flow<Boolean> = onMicCameraStateChangedListenerWrapper.isCameraCoveredStateFlow
        .onEach { logger.debug { "IsCameraCovered changed to $it" } }
        .flowOn(Dispatchers.Default)

    init {
        helper.binderStateFlow
            .filter { it == BinderState.CONNECTED }
            .onEach {
                logger.debug { "connected" }
                helper.execute { it.registerMicCameraStateListener(onMicCameraStateChangedListenerWrapper) }
            }
            .launchIn(coroutineScope)
        helper.connect()
    }

    @AnyThread
    override fun setCameraEnabled(newState: Boolean) {
        logger.debug { "setCameraEnabled: $newState" }
        coroutineScope.launch {
            helper.execute { service -> service.setCameraEnabled(newState) }
        }
    }

    @AnyThread
    override fun setMicEnabled(newState: Boolean) {
        logger.debug { "setMicEnabled: $newState" }
        coroutineScope.launch {
            helper.execute { service -> service.setMicEnabled(newState) }
        }
    }

    @Synchronized
    override fun dispose() {
        logger.debug { "dispose()" }
        coroutineScope.launch {
            logger.debug { "unregisterMicCameraStateListener()" }
            helper.execute { it.unregisterMicCameraStateListener(onMicCameraStateChangedListenerWrapper) }
            helper.disconnect()
            coroutineScope.cancel()
        }
    }
}
