@file:Suppress("unused", "WeakerAccess", "ForbidDefaultCoroutineDispatchers")
package ru.sberdevices.cv.detection

import android.content.Context
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import ru.sberdevices.common.binderhelper.BinderHelper2
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.cv.ICvDetectionService
import ru.sberdevices.cv.IDeathListener
import ru.sberdevices.cv.ServiceInfo
import ru.sberdevices.cv.api.BuildConfig
import ru.sberdevices.cv.detection.entity.IGestureDetectionListener
import ru.sberdevices.cv.detection.entity.IHumansDetectionListener
import ru.sberdevices.cv.detection.entity.IMirrorDetectedListener
import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.Humans
import ru.sberdevices.cv.detection.entity.humans.HumansDetectionAspect
import ru.sberdevices.cv.detection.util.toGesture
import ru.sberdevices.cv.detection.util.toHumans
import ru.sberdevices.cv.util.BindingIdStorage
import ru.sberdevices.cv.util.binderhelperlifecycle.BinderHelper2LifecycleEventsAdapter
import ru.sberdevices.cv.util.binderhelperlifecycle.entity.BinderLifecycleEvent.BINDING_DIED
import ru.sberdevices.cv.util.binderhelperlifecycle.entity.BinderLifecycleEvent.CONNECTED
import ru.sberdevices.cv.util.binderhelperlifecycle.entity.BinderLifecycleEvent.DISCONNECTED
import ru.sberdevices.cv.util.binderhelperlifecycle.entity.BinderLifecycleEvent.NULL_BINDING

private const val SERVICE_COMPONENT_PACKAGE = "ru.sberdevices.cv"
private const val SERVICE_COMPONENT_CLASS = "CvDetectionService"
private const val BINDING_CLOSING_DELAY_MS = 10L

private val serviceIntent = BinderHelper2.createBindIntent(
    packageName = SERVICE_COMPONENT_PACKAGE,
    className = "$SERVICE_COMPONENT_PACKAGE.$SERVICE_COMPONENT_CLASS"
)

internal class CvDetectionApiBinding(
    context: Context,
    private val bindingIdStorage: BindingIdStorage
) : CvApi {
    private val logger = Logger.get(javaClass.simpleName)

    private val bindingId = bindingIdStorage.bindingId

    private val binderHelper = BinderHelper2LifecycleEventsAdapter(context, serviceIntent) {
        ICvDetectionService.Stub.asInterface(it)
    }
    private val coroutineScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
                logger.error(throwable) { "coroutine $coroutineContext exception" }
            }
        )
    private val humanRecognitionsAspects = MutableStateFlow<Set<HumansDetectionAspect>>(emptySet())

    @Volatile
    private var callbacksCounter: Int = 0
        get() {
            logger.verbose { "get callbacks counter $field" }
            return field
        }
        set(value) {
            logger.verbose { "set callbacks counter $value" }
            field = value
        }

    @Volatile
    private var humansListener: IHumansDetectionListener.Stub? = null

    @Volatile
    private var lastHumans: Humans = Humans.EMPTY

    private val humansCallbackFlow = callbackFlow<Humans> {
        withBindingId { id, service ->
            logger.debug { "subscribe for humans detection" }
            ++callbacksCounter
            humansListener = object : IHumansDetectionListener.Stub() {
                override fun onUpdate(detectionEntity: ByteArray) {
                    val detection = detectionEntity.toHumans(humanRecognitionsAspects.value, lastHumans)
                    if (detection != null) lastHumans = detection
                    offer(detection ?: lastHumans)
                }
            }
            service.subscribeForHumansDetection(
                id,
                humansListener,
                humanRecognitionsAspects.value.map { it.code }.toByteArray()
            )
        }
        awaitClose {
            withBindingId { id, service ->

                if (humanRecognitionsAspects.value.isEmpty()) {
                    service.unsubscribeFromHumansDetection(id, humansListener)
                    logger.debug { "unsubscribe from humans detection" }
                }
            }
        }
    }

    private val humansSharedFlow = MutableSharedFlow<Humans>()

    @Volatile
    private var humansJob: Job? = null

    @Volatile
    private var gesturesListener: IGestureDetectionListener.Stub? = null
    private val gestureCallbackFlow = callbackFlow<Gesture> {
        withBindingId { id, service ->
            ++callbacksCounter
            gesturesListener = object : IGestureDetectionListener.Stub() {
                override fun onUpdate(detectionEntity: ByteArray) {
                    val detection = detectionEntity.toGesture()
                    if (detection != null) offer(detection)
                }
            }
            logger.debug { "subscribe for gestures detection" }
            service.subscribeForGestureDetection(id, gesturesListener)
        }
        awaitClose {
            withBindingId { id, service ->
                logger.debug { "unsubscribe from gestures detection" }
                service.unsubscribeFromGestureDetection(id, gesturesListener)
            }
        }
    }

    private val gestureSharedFlow = MutableSharedFlow<Gesture>()

    @Volatile
    private var gestureJob: Job? = null

    @Volatile
    private var isMirrorDetectedListener: IMirrorDetectedListener.Stub? = null
    private val isMirrorDetectedCallbackFlow = callbackFlow<Boolean> {
        withBindingId { id, service ->
            ++callbacksCounter
            isMirrorDetectedListener = object : IMirrorDetectedListener.Stub() {
                override fun onUpdate(detected: Boolean) {
                    offer(detected)
                }
            }
            logger.debug { "subscribe for mirror detection" }
            service.subscribeForIsMirrorDetected(id, isMirrorDetectedListener)
        }
        awaitClose {
            withBindingId { id, service ->
                logger.debug { "unsubscribe from mirror detection" }
                service.unsubscribeFromIsMirrorDetected(id, isMirrorDetectedListener)
            }
        }
    }
    private val isMirrorDetectedSharedFlow = MutableSharedFlow<Boolean>()

    @Volatile
    private var isMirrorDetectedJob: Job? = null

    init {
        subscribeToServiceConnectionLifecycleEvents()
        binderHelper.connect()
        generateBindingId()
        setupDeathListener()
        subscribeToHumansAspects()
    }

    private fun generateBindingId() {
        coroutineScope.launch {
            binderHelper.execute { service ->
                val storedBindingId = bindingIdStorage.get()
                if (storedBindingId == null) {
                    val generatedBindingId = service?.bindingId
                    if (generatedBindingId != null) {
                        bindingIdStorage.set(generatedBindingId)
                    }
                }
            }
        }
    }

    private fun setupDeathListener() {
        withBindingId { id, service ->
            service.sendDeathListener(
                id,
                object : IDeathListener.Stub() {
                    override fun onDeath(bindingId: Int) {
                        /** Do nothing */
                    }
                }
            )
            val version = BuildConfig.CV_API_VERSION
            logger.verbose { "sending client it cv api version to service: $version" }
            service.sendClientCvApiVersion(id, BuildConfig.CV_API_VERSION)
        }
    }

    private fun subscribeToHumansAspects() {
        humanRecognitionsAspects
            .onEach { aspects ->
                logger.debug { "aspects updated $aspects" }
                if (aspects.isEmpty()) {
                    humansJob?.cancel()
                    humansJob = null
                } else {
                    humansJob = humansCallbackFlow
                        .onEach { coroutineScope.launch { humansSharedFlow.emit(it) } }
                        .launchIn(coroutineScope)
                    humansJob?.invokeOnCompletion {
                        --callbacksCounter
                        logger.debug { "humans callback flow completed" }
                    }
                }
            }
            .onStart { logger.debug { "start humans aspects reaction" } }
            .onCompletion { logger.debug { "stop humans aspects reaction" } }
            .launchIn(coroutineScope)
    }

    private fun subscribeToServiceConnectionLifecycleEvents() {
        binderHelper.events.onEach { event ->
            when (event) {
                CONNECTED -> restoreDetection()
                DISCONNECTED,
                BINDING_DIED,
                NULL_BINDING -> {
                    isMirrorDetectedSharedFlow.emit(false)
                    humansSharedFlow.emit(Humans.EMPTY)
                }
            }
        }.launchIn(coroutineScope)
    }

    private fun restoreDetection() {
        logger.debug { "Connected to cv detection service" }
        val shouldRestoreHumansDetection = humansSharedFlow.subscriptionCount.value > 0 &&
            humansListener != null &&
            humanRecognitionsAspects.value.isNotEmpty()
        if (shouldRestoreHumansDetection) {
            withBindingId { id, service ->
                logger.debug {
                    "restoring detections for humans with aspects $humanRecognitionsAspects " +
                        "for binding id ${bindingId.value}"
                }
                service.subscribeForHumansDetection(
                    id,
                    humansListener,
                    humanRecognitionsAspects.value.map { it.code }.toByteArray()
                )
            }
        }
        if (gestureSharedFlow.subscriptionCount.value > 0 && gesturesListener != null) {
            withBindingId { id, service ->
                logger.debug { "restoring detections for gestures for binding id ${bindingId.value}" }
                service.subscribeForGestureDetection(
                    id,
                    gesturesListener
                )
            }
        }
        if (isMirrorDetectedSharedFlow.subscriptionCount.value > 0 && isMirrorDetectedListener != null) {
            withBindingId { id, service ->
                logger.debug { "restoring detections for mirror for binding id ${bindingId.value}" }
                service.subscribeForIsMirrorDetected(
                    id,
                    isMirrorDetectedListener
                )
            }
        }
    }

    override fun close() {
        logger.debug { "stop observing humans with aspects $humanRecognitionsAspects" }
        humanRecognitionsAspects.tryEmit(emptySet())
        humansSharedFlow.resetReplayCache()

        logger.debug { "stop observing gestures" }
        gestureJob?.cancel()
        gestureJob = null
        gestureSharedFlow.resetReplayCache()

        logger.debug { "stop observing mirror" }
        isMirrorDetectedJob?.cancel()
        isMirrorDetectedJob = null
        isMirrorDetectedSharedFlow.resetReplayCache()

        coroutineScope.launch {
            while (callbacksCounter != 0) {
                logger.debug { "trying close binding, but some active callbacks yet present" }
                delay(BINDING_CLOSING_DELAY_MS)
            }
            binderHelper.disconnect()
            coroutineScope.cancel()
            logger.info { "binding closed" }
        }
    }

    override suspend fun getVersion(): String? {
        logger.info { "get version" }
        return binderHelper.execute { service -> service.version }
    }

    override suspend fun getServiceInfo(): ServiceInfo? {
        logger.info { "get service info" }
        return binderHelper.execute { service -> service.serviceInfo }
    }

    override fun observeIsMirrorDetected(): Flow<Boolean> {
        return isMirrorDetectedSharedFlow
            .asSharedFlow()
            .onSubscription {
                logger.debug { "start observing mirror" }
                val job = isMirrorDetectedCallbackFlow
                    .onEach { isMirrorDetectedSharedFlow.emit(it) }
                    .launchIn(coroutineScope)
                job.invokeOnCompletion {
                    --callbacksCounter
                    logger.debug { "mirror callback flow completed" }
                }
                isMirrorDetectedJob = job
            }
            .onCompletion {
                logger.debug { "stop observing mirror" }
                isMirrorDetectedJob?.cancel()
                isMirrorDetectedJob = null
            }
    }

    override fun observeHumans(aspects: Set<HumansDetectionAspect>): Flow<Humans> {
        coroutineScope.launch { humanRecognitionsAspects.emit(aspects) }
        return humansSharedFlow.asSharedFlow()
            .onSubscription { logger.debug { "start observing humans with aspects $aspects" } }
            .onCompletion {
                humanRecognitionsAspects.emit(emptySet())
                logger.debug { "stop observing humans with aspects $aspects" }
            }
    }

    override fun observeGestures(): Flow<Gesture> {
        return gestureSharedFlow
            .asSharedFlow()
            .onSubscription {
                logger.debug { "start observing gestures" }
                val job = gestureCallbackFlow
                    .onEach { gestureSharedFlow.emit(it) }
                    .launchIn(coroutineScope)
                job.invokeOnCompletion {
                    --callbacksCounter
                    logger.debug { "gestures callback flow completed" }
                }
                gestureJob = job
            }
            .onCompletion {
                logger.debug { "stop observing gestures" }
                gestureJob?.cancel()
                gestureJob = null
            }
    }

    private fun withBindingId(action: (Int, ICvDetectionService) -> Unit) {
        bindingId
            .filterNotNull()
            .onEach { id ->
                binderHelper.execute { service ->
                    action.invoke(id, service)
                }
            }
            .launchIn(coroutineScope)
    }
}
