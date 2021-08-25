package ru.sberdevices.camera.view

import android.view.SurfaceView
import ru.sberdevices.camera.controller.CameraController

/**
 * Фабрика [CameraView].
 *
 * Пример создания:
 *  val cameraStateRepository = MicCameraStateRepositoryFactory.create(context)
 *  val cameraController = CameraControllerFactory.create(context, cameraStateRepository)
 */
object CameraViewFactory {

    fun create(
        surfaceView: SurfaceView,
        cameraController: CameraController,
    ): CameraView {
        return CameraViewImpl(surfaceView.holder, cameraController)
    }
}
