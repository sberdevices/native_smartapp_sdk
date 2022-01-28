package ru.sberdevices.services.mic.camera.state

import androidx.annotation.AnyThread
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow

/**
 * Repository of current state of device's camera and microphone.
 */
interface MicCameraStateRepository {

    /**
     * Current microphone state.
     */
    val micState: Flow<State>

    /**
     * Current camera state.
     */
    val cameraState: Flow<State>

    /**
     * Is camera covered now.
     */
    val isCameraCovered: Flow<Boolean>

    /**
     * Set camera state programmatically.
     *
     * Internal use only.
     * Requires permission ru.sberdevices.permission.CHANGE_CAMERA_STATE
     */
    @AnyThread
    @RequiresPermission("ru.sberdevices.permission.CHANGE_CAMERA_STATE")
    fun setCameraEnabled(newState: Boolean)

    /**
     * Set mic state programmatically.
     *
     * Internal use only.
     * Requires permission ru.sberdevices.permission.CHANGE_CAMERA_STATE
     */
    @AnyThread
    @RequiresPermission("ru.sberdevices.permission.CHANGE_MIC_STATE")
    fun setMicEnabled(newState: Boolean)

    /**
     * Disconnect and clear resources.
     */
    fun dispose()

    enum class State {
        /**
         * Turned on.
         */
        ENABLED,

        /**
         * Turned off.
         */
        DISABLED,

        /**
         * Unknown.
         */
        UNKNOWN,

        /**
         * The device has no camera or microphone.
         */
        NO_DEVICE;

        override fun toString(): String = when (this) {
            ENABLED -> "enabled"
            DISABLED -> "disabled"
            UNKNOWN -> "unknown"
            NO_DEVICE -> "no_device"
        }
    }
}
