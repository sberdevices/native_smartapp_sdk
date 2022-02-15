package ru.sberdevices.camera.view

/**
 * Отображает изображение с камеры.
 * Восстанавливает изображение при открытии крышки камеры.
 *
 * Пример создания:
 *  val cameraStateRepository = MicCameraStateRepositoryFactory.create(context)
 *  val cameraController = CameraControllerFactory.create(context, cameraStateRepository)
 *  val cameraView = CameraViewFactory.create(binding.drawView, cameraController)
 */
interface CameraView {

    companion object {
        const val DEFAULT_CAMERA_ID = "0"
    }
}
