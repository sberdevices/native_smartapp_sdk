package ru.sberdevices.camera.factories.session

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.view.Surface
import androidx.annotation.CheckResult
import ru.sberdevices.camera.factories.preview.PreviewCallback
import ru.sberdevices.camera.factories.snapshot.SnapshotCallback
import ru.sberdevices.camera.utils.CameraExceptionHandler

internal interface CameraSession {
    val surfaces: List<Surface>

    @CheckResult
    fun setRepeatingBurst(
        request: CaptureRequest,
        callback: PreviewCallback,
        handler: Handler
    ): Boolean

    fun capture(
        request: CaptureRequest,
        callback: SnapshotCallback,
        handler: Handler
    ): Boolean

    @CheckResult
    fun close(): Boolean
}

internal class CameraSessionWrapper(
    private val session: CameraCaptureSession,
    override val surfaces: List<Surface>,
    private val exceptionHandler: CameraExceptionHandler
) : CameraSession {
    override fun setRepeatingBurst(
        request: CaptureRequest,
        callback: PreviewCallback,
        handler: Handler
    ): Boolean = invokeSafe {
        session.setRepeatingBurst(listOf(request), callback, handler)
    } != null

    override fun capture(
        request: CaptureRequest,
        callback: SnapshotCallback,
        handler: Handler
    ): Boolean = invokeSafe {
        session.capture(request, callback, handler)
    } != null

    override fun close(): Boolean = invokeSafe {
        session.close()
    } != null

    private inline fun <T> invokeSafe(block: () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            exceptionHandler.sessionException(e)
            null
        }
    }
}
