package ru.sberdevices.services.mic.camera.state

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import androidx.annotation.AnyThread
import androidx.core.content.ContextCompat.checkSelfPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.sberdevices.common.binderhelper.BinderHelper2
import ru.sberdevices.common.binderhelper.BinderHelper2Factory
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.mic.camera.state.aidl.IMicCameraStateService
import ru.sberdevices.services.mic.camera.state.aidl.IOnMicCameraStateChangedListener

private val BIND_INTENT = BinderHelper2.createBindIntent(
    packageName = "ru.sberdevices.services",
    className = "ru.sberdevices.services.mic.camera.state.MicCameraStateService"
)

/**
 * Implementation [MicCameraStateRepository].
 */
internal class MicCameraStateRepositoryImpl(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : MicCameraStateRepository {

    private val logger by lazy { Logger.get("MicCameraStateRepositoryImpl") }

    private val _micState = MutableStateFlow(MicCameraStateRepository.State.UNKNOWN)
    private val _cameraState = MutableStateFlow(MicCameraStateRepository.State.UNKNOWN)
    private val _isCameraCoveredState = MutableStateFlow(false)

    override val micState: Flow<MicCameraStateRepository.State>
        get() = _micState
    override val cameraState: Flow<MicCameraStateRepository.State>
        get() = _cameraState
    override val isCameraCovered: Flow<Boolean>
        get() = _isCameraCoveredState

    private val micCameraStateChangedListener = object : IOnMicCameraStateChangedListener.Stub() {
        override fun onCameraMicStateChanged(
            isMicDisabled: Boolean,
            isCameraDisabled: Boolean,
            isCameraCovered: Boolean
        ) {
            _micState.value =
                if (isMicDisabled) MicCameraStateRepository.State.DISABLED else MicCameraStateRepository.State.ENABLED
            _cameraState.value =
                if (isCameraDisabled) MicCameraStateRepository.State.DISABLED else MicCameraStateRepository.State.ENABLED
            _isCameraCoveredState.value = isCameraCovered
        }
    }
    private val helper =
        BinderHelper2Factory.getBinderHelper2(context, BIND_INTENT) { IMicCameraStateService.Stub.asInterface(it) }

    init {
        helper.connect()
        coroutineScope.launch {
            logger.debug { "registerLocationListener()" }
            helper.execute { it.registerMicCameraStateListener(micCameraStateChangedListener) }
        }
    }

    @AnyThread
    override fun setCameraEnabled(newState: Boolean) {
        checkCanUpdateCameraState()
        logger.debug { "setCameraEnabled: $newState" }
        coroutineScope.launch {
            helper.execute { service -> service.setCameraEnabled(newState) }
        }
    }

    @Synchronized
    override fun dispose() {
        logger.debug { "dispose()" }
        coroutineScope.launch {
            logger.debug { "unregisterLocationListener()" }
            helper.execute { it.unregisterMicCameraStateListener(micCameraStateChangedListener) }
            helper.disconnect()
        }
    }

    private fun checkCanUpdateCameraState() {
        if (checkSelfPermission(context, "ru.sberdevices.permission.CHANGE_CAMERA_STATE") == PERMISSION_DENIED) {
            throw SecurityException("you don't have permission to CHANGE_CAMERA_STATE")
        }
    }
}
