package ru.sberdevices.camera.utils

import androidx.annotation.AnyThread
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import ru.sberdevices.common.logger.Logger

@AnyThread
interface CameraCoveredReceiver {
    fun register()
    fun unregister()
}

@AnyThread
class CameraCoveredReceiverImpl(
    private val cameraCoveredFlow: Flow<Boolean>,
    private val listener: CameraCoveredListener,
) : CameraCoveredReceiver {

    private val logger = Logger.get("CameraCoveredReceiver")
    private var isRegistered = false

    private val scope = MainScope() + CoroutineExceptionHandler { _, e ->
        logger.error(e) { "coroutine exception" }
    }

    @Synchronized
    override fun register() {
        logger.debug { "register isRegistered=$isRegistered" }
        if (!isRegistered) {
            isRegistered = true
            cameraCoveredFlow.onEach { isCovered ->
                logger.debug { "cameraCovered $isCovered" }
                if (isCovered) {
                    listener.onCameraCovered()
                } else {
                    listener.onCameraUncovered()
                }
            }.launchIn(scope)
        }
    }

    @Synchronized
    override fun unregister() {
        logger.debug { "unregister isRegistered=$isRegistered" }
        if (isRegistered) {
            isRegistered = false
            scope.coroutineContext.cancelChildren()
        }
    }
}
