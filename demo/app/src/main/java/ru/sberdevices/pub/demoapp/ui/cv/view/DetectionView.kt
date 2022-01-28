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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.sberdevices.camera.controller.CameraController
import ru.sberdevices.camera.controller.CameraControllerFactory
import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.BodyMask
import ru.sberdevices.cv.detection.entity.humans.Humans
import ru.sberdevices.cv.detection.entity.humans.Point
import ru.sberdevices.pub.demoapp.ui.cv.mapper.toPictureRes
import ru.sberdevices.services.pub.demoapp.databinding.ViewDetectionBinding

private const val CAMERA_ID = "0"
private const val BODY_MAKS_DISPLAY_WIDTH_PX = 256
private const val BODY_MASK_DISPLAY_HEIGHT_PX = 192

internal class DetectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewDetectionBinding.inflate(LayoutInflater.from(context), this)

    private val bodyMaskPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val humanPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 7f
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
        val controller: CameraController = CameraControllerFactory.create(context)
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
            val gestureRes = gesture.toPictureRes()
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
            drawOnCanvas { canvas ->
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

    fun clearCanvas() {
        val canvas = binding.infoView.holder.lockCanvas()
        if (canvas != null) {
            clearCanvas(canvas)
            binding.infoView.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun clearCanvas(canvas: Canvas) {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), clearPaint)
    }

    private inner class Connection(val startPointIndex: Int, val endPointIndex: Int) {
        fun draw(canvas: Canvas, paint: Paint, points: List<Point>) {
            val startX = points.getOrNull(startPointIndex)?.getAbsoluteX(binding.drawView.width)?.toFloat() ?: -1.0f
            val startY = points.getOrNull(startPointIndex)?.getAbsoluteY(binding.drawView.height)?.toFloat() ?: -1.0f
            val stopX = points.getOrNull(endPointIndex)?.getAbsoluteX(binding.drawView.width)?.toFloat() ?: -1.0f
            val stopY = points.getOrNull(endPointIndex)?.getAbsoluteY(binding.drawView.height)?.toFloat() ?: -1.0f
            if (startX > 0.0f && startY > 0.0f && stopX > 0.0f && stopY > 0.0f) {
                canvas.drawLine(startX, startY, stopX, stopY, paint)
            }
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
