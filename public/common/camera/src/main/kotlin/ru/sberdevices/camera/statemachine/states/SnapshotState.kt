package ru.sberdevices.camera.statemachine.states

import android.graphics.BitmapFactory
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.snapshot.SnapshotCapturedCallback
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine
import ru.sberdevices.common.logger.Logger

internal class SnapshotState(
    private val machine: CameraStateMachine,
    private val camera: Camera,
    private val callback: SnapshotCapturedCallback,
) : OpenedState(machine, camera), ImageReader.OnImageAvailableListener {

    private val logger = Logger.get("SnapshotState")

    private var captured = false
    private val imageReader = machine.createSnapshotImageReader(camera)

    override fun onAction(action: CameraAction) {
        when (action) {
            is CameraAction.Callback.PreviewStarted -> {
                /* ignore */
            }
            is CameraAction.Callback.SessionConfigured -> {
                val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                if (request != null) {
                    val session = action.session
                    for (surface in session.surfaces) {
                        request.addTarget(surface)
                    }
                    request.set(CaptureRequest.JPEG_ORIENTATION, camera.getJpegOrientation())
                    val captureStarted = session.capture(
                        request.build(),
                        machine.createSnapshotCallback(),
                        machine.cameraHandler
                    )
                    if (!captureStarted) {
                        logger.warn { "capture isn't started" }
                        machine.state = ReopeningState(machine, camera)
                    }
                } else {
                    logger.warn { "request isn't created" }
                    machine.state = ReopeningState(machine, camera)
                }
            }
            else -> super.onAction(action)
        }
    }

    override fun onImageAvailable(reader: ImageReader) {
        if (!captured) {
            var image: Image? = null
            try {
                image = reader.acquireLatestImage()
                if (image != null) {
                    val plane = image.planes[0]
                    val buffer = plane.buffer
                    val data = ByteArray(buffer.capacity())
                    buffer.get(data)
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    image.close()
                    captured = true
                    machine.snapshotRequest = null
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        callback.onCaptured(bitmap)
                    }
                    machine.state = StartingSessionState(machine, camera)
                }
            } catch (e: Exception) {
                logger.error(e) { "image capturing error" }
                image?.close()
            }
        }
    }

    override fun onEnter() {
        val sessionCreated = machine.openSession(camera, machine.surfaces + imageReader.surface)
        if (!sessionCreated) {
            logger.warn { "session wasn't created" }
            machine.state = ReopeningState(machine, camera)
        }
        imageReader.setOnImageAvailableListener(this, machine.cameraHandler)
    }

    override fun onLeave() {
        imageReader.close()
    }
}
