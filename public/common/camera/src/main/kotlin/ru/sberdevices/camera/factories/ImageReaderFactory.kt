package ru.sberdevices.camera.factories

import android.graphics.ImageFormat
import android.graphics.Point
import android.media.ImageReader
import android.util.Size
import android.view.WindowManager
import ru.sberdevices.camera.factories.camera.Camera

internal interface ImageReaderFactory {
    fun create(camera: Camera): ImageReader
}

internal class ImageReaderFactoryImpl(windowManager: WindowManager) : ImageReaderFactory {

    private val previewSize: Size

    init {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        previewSize = Size(point.x, point.y)
    }

    override fun create(camera: Camera): ImageReader {
        val size = camera.getBestSize(previewSize)
        return ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 2)
    }
}
