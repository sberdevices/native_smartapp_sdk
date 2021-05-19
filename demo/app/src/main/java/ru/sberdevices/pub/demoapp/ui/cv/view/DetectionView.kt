@file:Suppress("ForbidDefaultCoroutineDispatchers")
package ru.sberdevices.pub.demoapp.ui.cv.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.applyCanvas
import androidx.core.view.isVisible
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.sberdevices.camera.controller.CameraController
import ru.sberdevices.camera.controller.CameraStarterFactory
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.BodyMask
import ru.sberdevices.cv.detection.entity.humans.Humans
import ru.sberdevices.cv.detection.entity.humans.Point
import ru.sberdevices.cv.entity.BoundingBox
import ru.sberdevices.cv.entity.BoundingBox.Companion.fromAbsolute
import ru.sberdevices.pub.demoapp.ui.cv.mapper.toPictureRes
import ru.sberdevices.services.pub.demoapp.databinding.ViewDetectionBinding
import kotlin.math.max
import kotlin.math.min

private const val CV_SERVICE_CAMERA_WIDTH_PX = 1920
private const val CV_SERVICE_CAMERA_HEIGHT_PX = 1080
private const val CAMERA_ID = "0"
private const val BODY_MAKS_DISPLAY_WIDTH_PX = 256
private const val BODY_MASK_DISPLAY_HEIGHT_PX = 192

internal class DetectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewDetectionBinding.inflate(LayoutInflater.from(context), this)

    private val logger by Logger.lazy(javaClass.simpleName)

    private val bodyMaskPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val humanPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 7f
        style = Paint.Style.STROKE
    }
    private val palmBoundingBoxPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val posePaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 5f
    }
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val connections = listOf(
        Connection(1, 2), // head line
        Connection(5, 6), // Shoulders
        Connection(5, 7), Connection(7, 9), // left arm
        Connection(6, 8), Connection(8, 10), // right arm
        Connection(11, 12), // hips
        Connection(5, 11), Connection(11, 13), Connection(13, 15), // left leg
        Connection(6, 12), Connection(12, 14), Connection(14, 16), // right leg
    )

    private var surfaceTexture: SurfaceTexture? = null

    private val cameraController: CameraController = createCameraController()
    var cameraPermissionGranted = false
        set(value) {
            if (value) {
                cameraController.start(CAMERA_ID, listOf(Surface(surfaceTexture)))
            }
            field = value
        }

    init {
        binding.infoView.setZOrderOnTop(true)
        binding.infoView.holder.setFormat(PixelFormat.TRANSPARENT)
    }

    private fun createCameraController(): CameraController {
        val controller: CameraController = CameraStarterFactory.create(context)
        binding.drawView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                this@DetectionView.surfaceTexture = surfaceTexture
                if (cameraPermissionGranted) {
                    cameraController.start(CAMERA_ID, listOf(Surface(surfaceTexture)))
                }
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                this@DetectionView.surfaceTexture = null
                cameraController.stop()
                return true
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) = Unit

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
        }
        return controller
    }

    fun drawHumans(humans: Humans) {
        humans.bodyMask?.let { drawBodyMask(it) }
        if (humans.isFilled()) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val boundingBoxes = humans.bodyBoundingBoxes?.boundingBoxes.orEmpty() +
                humans.faceBoundingBoxes?.boundingBoxes.orEmpty()
            val rects = boundingBoxes.map { it.asAbsoluteRect(width, height) }
            val points = humans.faceLandmarks?.pointGroups?.flatMap { it.points }.orEmpty() +
                humans.bodyLandmarks?.pointGroups?.flatMap { it.points }.orEmpty()
            val poses = humans.bodyLandmarks?.pointGroups.orEmpty()

            bitmap.applyCanvas {
                rects.forEach { drawRect(it, humanPaint) }
                points.forEach {
                    drawPoint(
                        it.getAbsoluteX(width).toFloat(),
                        it.getAbsoluteY(height).toFloat(),
                        humanPaint
                    )
                }
                poses.forEach { pose ->
                    connections.forEach { connection ->
                        connection.draw(this@applyCanvas, posePaint, pose.points)
                    }
                }
            }
            GlobalScope.launch(Dispatchers.Main.immediate) {
                drawOnCanvas { canvas ->
                    canvas.drawBitmap(bitmap, null, Rect(0, 0, width, height), posePaint)
                }
            }
        }
    }

    private fun Humans.isFilled(): Boolean {
        return bodyBoundingBoxes != null ||
            faceBoundingBoxes != null ||
            bodyLandmarks != null ||
            faceLandmarks != null
    }

    fun drawMirror(detected: Boolean?) {
        binding.mirrorImageView.isVisible = detected != null
        if (detected != null) {
            binding.mirrorImageView.setColorFilter(if (detected) Color.GREEN else Color.RED)
        }
    }

    fun drawGesture(gesture: Gesture) {
        GlobalScope.launch(Dispatchers.IO) {
            val palmBoundingBox = extractPalmBoundingBox(gesture)
            val gestureRes = gesture.toPictureRes()
            drawOnCanvas { canvas ->
                if (palmBoundingBox != null) canvas.drawRect(
                    palmBoundingBox.asAbsoluteRect(binding.drawView.width, binding.drawView.height),
                    palmBoundingBoxPaint
                )
            }
            withContext(Dispatchers.Main) {
                if (gestureRes != null) {
                    binding.gestureImageView.setImageResource(gestureRes)
                } else {
                    binding.gestureImageView.setImageDrawable(null)
                }
                binding.gestureImageView.isVisible = gestureRes != null
            }
        }
    }

    fun clearGesture() {
        binding.gestureImageView.setImageDrawable(null)
    }

    @Suppress("TooGenericExceptionCaught", "ComplexCondition") // Нужно отлавливать любые ошибки
    private fun extractPalmBoundingBox(gesture: Gesture): BoundingBox? {
        return try {
            val x = extractIntValueFromJson(gesture.metadata, "StaticGesture/BoundingBox/x")
            val y = extractIntValueFromJson(gesture.metadata, "StaticGesture/BoundingBox/y")
            val widthPx = extractIntValueFromJson(
                gesture.metadata, "StaticGesture/BoundingBox/width"
            )
            val heightPx = extractIntValueFromJson(
                gesture.metadata, "StaticGesture/BoundingBox/height"
            )
            if (x != null && y != null && widthPx != null && heightPx != null) {
                getPalmBoundingBox(x, y, widthPx, heightPx)
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error(e) { "Can't extract palm bounding box from gesture metadata" }
            null
        }
    }

    private fun getPalmBoundingBox(x: Int, y: Int, widthPx: Int, heightPx: Int): BoundingBox {
        return fromAbsolute(
            normalizeCoordinate(x, CV_SERVICE_CAMERA_WIDTH_PX),
            normalizeCoordinate(y, CV_SERVICE_CAMERA_HEIGHT_PX),
            normalizeCoordinate(x + widthPx, CV_SERVICE_CAMERA_WIDTH_PX),
            normalizeCoordinate(y + heightPx, CV_SERVICE_CAMERA_HEIGHT_PX),
            CV_SERVICE_CAMERA_WIDTH_PX,
            CV_SERVICE_CAMERA_HEIGHT_PX
        )
    }

    private fun normalizeCoordinate(value: Int, maxValue: Int): Int {
        return max(0, min(value, maxValue))
    }

    @Suppress("ReturnCount") // TODO Refactor
    private fun extractIntValueFromJson(json: String?, path: String?): Int? {
        if (json.isNullOrBlank() || path.isNullOrBlank()) return null

        var jsonElement = JsonParser.parseString(json)
        for (pathElement in path.split("/".toRegex()).toTypedArray()) {
            if (jsonElement == null) {
                return null
            }
            val jsonObject = jsonElement.asJsonObject ?: return null
            jsonElement = jsonObject[pathElement]
        }
        return jsonElement?.asInt
    }

    private fun drawBodyMask(bodyMask: BodyMask) {
        val (mask, heightPx, widthPx) = bodyMask
        val colors = mask.foldRightIndexed(
            IntArray(mask.size),
            { index, value, accumulator ->
                accumulator[index] = value.toUnsignedInt().toARGBColor()
                accumulator
            }
        )
        val bitmap = Bitmap.createBitmap(colors, widthPx, heightPx, Bitmap.Config.ARGB_8888)
        GlobalScope.launch(Dispatchers.Main.immediate) {
            drawOnCanvas(clear = false) { canvas ->
                canvas.drawBitmap(
                    bitmap,
                    null,
                    Rect(0, 0, BODY_MAKS_DISPLAY_WIDTH_PX, BODY_MASK_DISPLAY_HEIGHT_PX),
                    bodyMaskPaint
                )
            }
        }
    }

    private suspend fun drawOnCanvas(clear: Boolean = true, drawing: (Canvas) -> Unit) {
        withContext(Dispatchers.Main) {
            val canvas = binding.infoView.holder.lockCanvas()
            if (canvas != null) {
                if (clear) clearCanvas(canvas)
                drawing.invoke(canvas)
                binding.infoView.holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun clearCanvas(canvas: Canvas) {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), clearPaint)
    }

    private inner class Connection(val startPointIndex: Int, val endPointIndex: Int) {
        fun draw(canvas: Canvas, paint: Paint, points: List<Point>) {
            canvas.drawLine(
                points.getOrNull(startPointIndex)?.getAbsoluteX(binding.drawView.width)?.toFloat()
                    ?: 0f,
                points.getOrNull(startPointIndex)?.getAbsoluteY(binding.drawView.height)?.toFloat()
                    ?: 0f,
                points.getOrNull(endPointIndex)?.getAbsoluteX(binding.drawView.width)?.toFloat()
                    ?: 0f,
                points.getOrNull(endPointIndex)?.getAbsoluteY(binding.drawView.height)?.toFloat()
                    ?: 0f,
                paint
            )
        }
    }
}

private fun Byte.toUnsignedInt(): Int {
    return toInt() and 0xFF
}

@ColorInt
private fun Int.toARGBColor(): Int {
    return -0x1000000 + 0x00010000 * this + 0x00000100 * this + this
}
