package ru.sberdevices.camera.controller

import android.os.Handler
import android.view.Surface
import androidx.annotation.AnyThread
import kotlinx.coroutines.flow.Flow
import ru.sberdevices.camera.factories.camera.CameraInfoProvider
import ru.sberdevices.camera.factories.snapshot.SnapshotCapturedCallback
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.camera.utils.CameraCoveredReceiver
import ru.sberdevices.common.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean

internal class CameraControllerImpl(
    private val stateMachine: CameraStateMachine,
    private val coveredReceiver: CameraCoveredReceiver,
    private val cameraHandler: Handler
) : CameraController {

    private val logger = Logger.get("CameraStarterImpl")

    private val isReleased = AtomicBoolean(false)

    override val cameraInfoProvider: Flow<CameraInfoProvider> = stateMachine.cameraInfoProvider

    init {
        coveredReceiver.register()
    }

    @AnyThread
    override fun start(cameraId: String, surfaces: List<Surface>) {
        logger.debug { "start $cameraId" }
        if (!isReleased.get()) {
            cameraHandler.post {
                stateMachine.start(cameraId, surfaces)
            }
        } else {
            logger.warn { "start called in released state" }
        }
    }

    override fun snapshot(callback: SnapshotCapturedCallback) {
        if (!isReleased.get()) {
            cameraHandler.post {
                stateMachine.snapshot(callback)
            }
        } else {
            logger.warn { "snapshot called in released state" }
        }
    }

    @AnyThread
    override fun stop() {
        logger.debug { "stop" }
        if (!isReleased.get()) {
            cameraHandler.post {
                stateMachine.stop()
            }
        } else {
            logger.warn { "stop called in released state" }
        }
    }

    override fun release() {
        logger.debug { "release" }
        coveredReceiver.unregister()
        if (isReleased.compareAndSet(false, true)) {
            cameraHandler.post {
                stateMachine.stop()
                cameraHandler.removeCallbacksAndMessages(null)
                cameraHandler.looper.quitSafely()
            }
        }
    }
}
