package ru.sberdevices.camera.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import ru.sberdevices.camera.R
import ru.sberdevices.camera.controller.CameraController
import ru.sberdevices.camera.controller.CameraStarterFactory

/**
 * Renders camera texture on itself.
 */
class CameraView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs) {
    private var mCameraController: CameraController? = null
    val cameraId: String

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CameraView)
        try {
            cameraId = attributes.getInt(R.styleable.CameraView_cameraId, DEFAULT_CAMERA_ID).toString()
        } finally {
            attributes.recycle()
        }
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val cameraStarter = CameraStarterFactory.create(context)
                cameraStarter.start(cameraId, listOf(holder.surface))
                this@CameraView.mCameraController = cameraStarter
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mCameraController?.release()
                mCameraController = null
            }
        })
    }

    private companion object {
        private const val DEFAULT_CAMERA_ID = 0
    }
}
