package ru.sberdevices.pub.demoapp.ui.cv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.cv.detection.CvApi
import ru.sberdevices.cv.detection.CvApiFactory
import ru.sberdevices.pub.demoapp.ui.cv.entity.Control
import ru.sberdevices.pub.demoapp.ui.cv.entity.DetectionEvent
import ru.sberdevices.pub.demoapp.ui.cv.entity.GestureDetectionEvent
import ru.sberdevices.pub.demoapp.ui.cv.entity.HumansDetectionEvent
import ru.sberdevices.pub.demoapp.ui.cv.mapper.toDomainHumansDetectionAspect
import ru.sberdevices.pub.demoapp.ui.cv.util.delegate

internal class ComputerVisionViewModel(
    private val cvApiFactory: CvApiFactory,
    private val ioCoroutineDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val logger by Logger.lazy(javaClass.simpleName)
    private var gesturesJob: Job? = null
    private var humansJob: Job? = null
    private var mirrorJob: Job? = null
    private var _state = MutableStateFlow(createInitialState())
    private var state by _state.delegate()

    val mirrorState = _state.map { it.isMirrorDetected }.distinctUntilChanged()

    private var _detections = MutableSharedFlow<DetectionEvent>()
    val detections = _detections.asSharedFlow().conflate()

    @Volatile
    private var cvApi: CvApi? = null

    init {
        requestServiceInfoAndVersion()
    }

    fun permissionsGranted() {
        logger.debug { "Permissions granted" }
        state = state.copy(permissionsGranted = true)
        cvApi = cvApiFactory.get()
    }

    fun resumed() {
        logger.debug { "Resumed" }
        if (state.permissionsGranted && cvApi == null) {
            cvApi = cvApiFactory.get()
        }
    }

    fun paused() {
        logger.debug { "Paused" }
        cvApi?.close()
        cvApi = null
    }

    private fun requestServiceInfoAndVersion() {
        viewModelScope.launch(ioCoroutineDispatcher) {
            val version = cvApi?.getVersion()
            val serviceInfo = cvApi?.getServiceInfo()
            logger.debug { "Version $version, service info $serviceInfo}" }
        }
    }

    fun humansAspectSwitched(enabled: Boolean, changedControl: Control) {
        logger.verbose { "${changedControl.javaClass.simpleName} ${if (enabled) "enabled" else "disabled"}" }
        val previousAspects = state.enabledAspects
        val aspects = if (enabled) previousAspects + changedControl else previousAspects - changedControl
        state = state.copy(enabledAspects = aspects)
        when {
            humansJob != null && humansJob?.isActive == true && aspects.isEmpty() -> humansJob?.cancel()
            humansJob != null && humansJob?.isActive == true && aspects.isNotEmpty() -> {
                humansJob?.invokeOnCompletion { subscribeToHumans(aspects) }
                humansJob?.cancel()
            }
            (humansJob == null || humansJob?.isActive != true) && aspects.isNotEmpty() -> subscribeToHumans(aspects)
        }
    }

    private fun subscribeToHumans(aspects: Set<Control>) {
        logger.debug { "subscribe to humans with aspects $aspects" }
        humansJob = cvApi?.observeHumans(aspects.mapNotNull { it.toDomainHumansDetectionAspect() }.toSet())
            ?.onEach { detection -> offerDetectionEvent(HumansDetectionEvent(detection)) }
            ?.catch { logger.error(it) { "humans job error" } }
            ?.onCompletion { logger.verbose { "humans job completed" } }
            ?.launchIn(viewModelScope)
    }

    fun gesturesSwitched(enabled: Boolean) {
        logger.verbose { "Gestures ${if (enabled) "enabled" else "disabled"}" }
        if (enabled) {
            gesturesJob = cvApi?.observeGestures()
                ?.cancellable()
                ?.onEach { gesture -> offerDetectionEvent(GestureDetectionEvent(gesture)) }
                ?.onCompletion { logger.verbose { "gestures job completed" } }
                ?.launchIn(viewModelScope)
        } else {
            gesturesJob?.cancel()
        }
    }

    fun mirrorSwitched(enabled: Boolean) {
        logger.verbose { "Mirror ${if (enabled) "enabled" else "disabled"}" }
        if (enabled) {
            mirrorJob = cvApi?.observeIsMirrorDetected()
                ?.cancellable()
                ?.onEach { state = state.copy(isMirrorDetected = it) }
                ?.onCompletion { logger.verbose { "mirror job completed" } }
                ?.launchIn(viewModelScope)
        } else {
            mirrorJob?.cancel()
            state = state.copy(isMirrorDetected = null)
        }
    }

    private fun offerDetectionEvent(event: DetectionEvent) {
        viewModelScope.launch(ioCoroutineDispatcher) { _detections.emit(event) }
    }

    override fun onCleared() {
        cvApi?.close()
        cvApi = null
        super.onCleared()
    }

    private fun createInitialState(): ComputerVisionViewState {
        return ComputerVisionViewState(
            enabledAspects = emptySet(),
            isMirrorDetected = null,
            permissionsGranted = false
        )
    }
}
