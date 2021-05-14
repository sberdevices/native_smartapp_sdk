package ru.sberdevices.camera.utils

import androidx.annotation.AnyThread
import ru.sberdevices.common.logger.Logger
import java.lang.Exception

@AnyThread
interface CameraExceptionHandler {
    fun cameraException(e: Exception)
    fun sessionException(e: Exception)
}

internal class CameraExceptionHandlerImpl : CameraExceptionHandler {
    private val logger = Logger.get("CameraExceptionHandler")

    override fun cameraException(e: Exception) {
        logger.error(e) { "Camera exception" }
    }

    override fun sessionException(e: Exception) {
        logger.error(e) { "Session exception" }
    }
}
