package ru.sberdevices.camera.factories.camera

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.util.Size
import android.view.Surface
import androidx.annotation.AnyThread
import ru.sberdevices.common.logger.Logger

@AnyThread
internal class CameraInfoProviderImpl(
    private val cameraId: String,
    manager: CameraManager,
    private val deviceOrientation: Int
) : CameraInfoProvider {
    private val logger = Logger.get("CameraInfoProviderImpl")

    private val characteristics = manager.getCameraCharacteristics(cameraId)

    override fun getJpegOrientation(): Int {
        val surfaceRotation = orientationDegrees.getValue(deviceOrientation)
        return (surfaceRotation + getSensorOrientation() + 270) % 360
    }

    override fun getSensorOrientation(): Int {
        val orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        return orientation ?: kotlin.run {
            logger.warn { "SENSOR_ORIENTATION property does not present for cameraId$cameraId" }
            0
        }
    }

    override fun getBestSize(maxSize: Size): Size {
        val params = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
        val outputSizes = params.getOutputSizes(ImageReader::class.java)
        return outputSizes.firstOrNull {
            it.width <= maxSize.width && it.height <= maxSize.height
        } ?: outputSizes[0]
    }

    override fun getMaxSize(): Size {
        val params = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
        return params.getOutputSizes(ImageReader::class.java)[0]
    }

    private companion object {
        private val orientationDegrees = HashMap<Int, Int>(4)

        init {
            orientationDegrees[Surface.ROTATION_0] = 90
            orientationDegrees[Surface.ROTATION_90] = 0
            orientationDegrees[Surface.ROTATION_180] = 270
            orientationDegrees[Surface.ROTATION_270] = 180
        }
    }
}
