package ru.sberdevices.camera.statemachine

import android.Manifest
import android.media.ImageReader
import android.os.Handler
import android.view.Surface
import androidx.annotation.AnyThread
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import ru.sberdevices.camera.factories.ImageReaderFactory
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.camera.CameraInfoProvider
import ru.sberdevices.camera.factories.camera.CameraOpener
import ru.sberdevices.camera.factories.preview.PreviewCallback
import ru.sberdevices.camera.factories.preview.PreviewCallbackFactory
import ru.sberdevices.camera.factories.session.SessionOpener
import ru.sberdevices.camera.factories.snapshot.SnapshotCallback
import ru.sberdevices.camera.factories.snapshot.SnapshotCallbackFactory
import ru.sberdevices.camera.factories.snapshot.SnapshotCapturedCallback
import ru.sberdevices.camera.factories.snapshot.SnapshotRequest
import ru.sberdevices.camera.utils.CameraExceptionHandler
import ru.sberdevices.common.logger.Logger

@WorkerThread
internal interface CameraStateMachine : StateHolder {
    val cameraInfoProvider: Flow<CameraInfoProvider>

    val cameraHandler: Handler

    val exceptionHandler: CameraExceptionHandler

    var surfaces: MutableList<Surface>

    var cameraId: String

    var snapshotRequest: SnapshotRequest?

    fun start(cameraId: String, surfaces: List<Surface>)

    fun stop()

    fun snapshot(callback: SnapshotCapturedCallback)

    fun illegalAction(action: CameraAction)

    @RequiresPermission(Manifest.permission.CAMERA)
    fun openCamera()

    fun openSession(camera: Camera, surfaces: List<Surface>): Boolean

    fun createPreviewCallback(): PreviewCallback

    fun createSnapshotCallback(): SnapshotCallback

    fun createSnapshotImageReader(camera: Camera): ImageReader
}

internal class CameraStateMachineImpl @AnyThread constructor(
    stateHolder: StateHolder,
    private val dispatcher: ActionDispatcher,
    private val cameraOpener: CameraOpener,
    private val sessionOpener: SessionOpener,
    private val previewCallbackFactory: PreviewCallbackFactory,
    private val snapshotCallbackFactory: SnapshotCallbackFactory,
    private val imageReaderFactory: ImageReaderFactory,
    override val cameraHandler: Handler,
    override val exceptionHandler: CameraExceptionHandler
) : CameraStateMachine, StateHolder by stateHolder {
    private val logger = Logger.get("CameraStateMachine")

    private val _cameraInfoProvider = MutableStateFlow<CameraInfoProvider?>(null)

    override val cameraInfoProvider: Flow<CameraInfoProvider>
        get() = _cameraInfoProvider.filterNotNull()

    override var surfaces = mutableListOf<Surface>()
    override var cameraId = "0"
    override var snapshotRequest: SnapshotRequest? = null

    private var isStarted = false

    override fun start(cameraId: String, surfaces: List<Surface>) {
        if (isStarted) {
            dispatcher.dispatch(action = CameraAction.Stop)
        }
        this.cameraId = cameraId
        this.surfaces.apply {
            clear()
            addAll(surfaces)
        }
        isStarted = true
        dispatcher.dispatch(action = CameraAction.Start)
    }

    override fun stop() {
        isStarted = false
        snapshotRequest = null
        surfaces.clear()
        dispatcher.dispatch(action = CameraAction.Stop)
    }

    override fun snapshot(callback: SnapshotCapturedCallback) {
        snapshotRequest = SnapshotRequest(callback)
        dispatcher.dispatch(action = CameraAction.Snapshot(callback))
    }

    override fun illegalAction(action: CameraAction) {
        logger.warn { "Illegal action:[${action.javaClass.simpleName}] state:[$state]" }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun openCamera() {
        logger.debug { "openCamera" }
        cameraOpener.openCamera(cameraId)
    }

    override fun openSession(camera: Camera, surfaces: List<Surface>): Boolean {
        logger.debug { "openSession" }
        _cameraInfoProvider.value = camera
        return sessionOpener.openSession(camera, surfaces)
    }

    override fun createPreviewCallback(): PreviewCallback {
        logger.debug { "createPreviewCallback" }
        return previewCallbackFactory.create()
    }

    override fun createSnapshotCallback(): SnapshotCallback {
        logger.debug { "createSnapshotCallback" }
        return snapshotCallbackFactory.create()
    }

    override fun createSnapshotImageReader(camera: Camera): ImageReader {
        logger.debug { "createSnapshotImageReader" }
        return imageReaderFactory.create(camera)
    }
}
