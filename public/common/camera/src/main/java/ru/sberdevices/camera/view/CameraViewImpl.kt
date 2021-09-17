package ru.sberdevices.camera.view

import android.view.SurfaceHolder
import ru.sberdevices.camera.controller.CameraController

internal class CameraViewImpl(
    private val surfaceHolder: SurfaceHolder,
    private val cameraController: CameraController,
) : CameraView {

    private var isStarted = false

    private val callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            start()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            stop()
            start()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            stop()
            release()
        }
    }

    init {
        surfaceHolder.addCallback(callback)
        if (!surfaceHolder.isCreating) {
            start()
        }
    }

    private fun start() {
        if (isStarted) return
        if (surfaceHolder.surface.isValid) {
            cameraController.start(CameraView.DEFAULT_CAMERA_ID, listOf(surfaceHolder.surface))
            isStarted = true
        }
    }

    private fun stop() {
        if (!isStarted) return
        cameraController.stop()
        isStarted = false
    }

    private fun release() {
        if (!isStarted) return
        cameraController.release()
        isStarted = false
    }
}
