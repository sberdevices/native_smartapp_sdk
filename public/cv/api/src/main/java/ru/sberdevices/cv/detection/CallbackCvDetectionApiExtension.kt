package ru.sberdevices.cv.detection

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.Humans
import ru.sberdevices.cv.detection.entity.humans.HumansDetectionAspect

/**
 * Allows clients written in java subscribe to flow CvApi without touching coroutines but only with
 * callback.
 */
interface CallbackCvDetectionApiExtension {
    fun subscribeForHumans(aspects: Set<HumansDetectionAspect>, humansListener: (Humans) -> Unit)
    fun unsubscribeFromHumans()
    fun subscribeForGestures(gestureListener: (Gesture) -> Unit)
    fun unsubscribeFromGestures()
    fun subscribeForMirrorState(mirrorStateListener: (Boolean) -> Unit)
    fun unsubscribeFromMirrorState()
    fun close()
}

class CallbackCvDetectionApiExtensionImpl(private val cvApi: CvApi) : CallbackCvDetectionApiExtension {
    private val logger by Logger.lazy(javaClass.simpleName)
    private val coroutineScope = CoroutineScope(
        CoroutineName(javaClass.simpleName) +
            Dispatchers.IO +
            SupervisorJob() +
            CoroutineExceptionHandler { coroutineContext, throwable ->
                logger.error(throwable) { "coroutine $coroutineContext exception" }
            }
    )

    @Volatile
    private var humansJob: Job? = null

    @Volatile
    private var gesturesJob: Job? = null

    @Volatile
    private var mirrorStateJob: Job? = null

    override fun subscribeForHumans(aspects: Set<HumansDetectionAspect>, humansListener: (Humans) -> Unit) {
        humansJob = cvApi.observeHumans(aspects)
            .onEach { humansListener.invoke(it) }
            .launchIn(coroutineScope)
        humansJob?.invokeOnCompletion { logger.debug { "observe humans completed" } }
    }

    override fun subscribeForGestures(gestureListener: (Gesture) -> Unit) {
        gesturesJob = cvApi.observeGestures()
            .onEach { gestureListener.invoke(it) }
            .launchIn(coroutineScope)
        gesturesJob?.invokeOnCompletion { logger.debug { "observe gestures completed" } }
    }

    override fun unsubscribeFromHumans() {
        humansJob?.cancel()
    }

    override fun unsubscribeFromGestures() {
        gesturesJob?.cancel()
    }

    override fun subscribeForMirrorState(mirrorStateListener: (Boolean) -> Unit) {
        mirrorStateJob = cvApi.observeIsMirrorDetected()
            .onEach { mirrorStateListener.invoke(it) }
            .launchIn(coroutineScope)
        mirrorStateJob?.invokeOnCompletion { logger.debug { "observe mirror completed" } }
    }

    override fun unsubscribeFromMirrorState() {
        mirrorStateJob?.cancel()
    }

    override fun close() {
        coroutineScope.cancel()
        cvApi.close()
    }
}
