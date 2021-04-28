package ru.sberdevices.services.mic.camera.state

import androidx.annotation.AnyThread
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий состояний камеры и микрофона.
 */
interface MicCameraStateRepository {

    /**
     * Флоу текущего состояния микрофона.
     */
    val micState: Flow<State>

    /**
     * Флоу текущего состояния камеры.
     */
    val cameraState: Flow<State>

    /**
     * Накрыта ли сейчас камера.
     */
    val isCameraCovered: Flow<Boolean>

    /**
     * Выключить камеру программно.
     *
     * Метод только для внутренного использования.
     * Требует пермишен ru.sberdevices.permission.CHANGE_CAMERA_STATE
     */
    @AnyThread
    @RequiresPermission("ru.sberdevices.permission.CHANGE_CAMERA_STATE")
    fun setCameraEnabled(newState: Boolean)

    /**
     * Отключиться от сервиса.
     */
    fun dispose()

    enum class State {
        /**
         * Включено.
         */
        ENABLED,

        /**
         * Выключено.
         */
        DISABLED,

        /**
         * Неизвестно.
         */
        UNKNOWN,

        /**
         * Отсутствует устройство (напр. камера или микрофон).
         */
        NO_DEVICE
    }
}
