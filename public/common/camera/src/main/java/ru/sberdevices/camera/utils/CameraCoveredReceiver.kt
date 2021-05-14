package ru.sberdevices.camera.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import ru.sberdevices.common.logger.Logger

@AnyThread
interface CameraCoveredReceiver {
    fun register()
    fun unregister()
}

@AnyThread
class CameraCoveredReceiverImpl(
    private val context: Context,
    private val listener: CameraCoveredListener
) : BroadcastReceiver(), CameraCoveredReceiver {

    private val logger = Logger.get("CameraCoveredReceiver")
    private val coverIntentFilter = IntentFilter("android.intent.action.CAMERA_COVER")
    private var isRegistered = false

    @MainThread
    override fun onReceive(context: Context, intent: Intent) {
        if (isRegistered) {
            val isCameraCovered = intent.getBooleanExtra(COVER_STATE, false)
            if (isCameraCovered) {
                listener.onCameraCovered()
            } else {
                listener.onCameraUncovered()
            }
        }
    }

    @Synchronized
    override fun register() {
        logger.debug { "register isRegistered=$isRegistered" }
        if (!isRegistered) {
            context.registerReceiver(this, coverIntentFilter)
            isRegistered = true
        }
    }

    @Synchronized
    override fun unregister() {
        logger.debug { "unregister isRegistered=$isRegistered" }
        if (isRegistered) {
            context.unregisterReceiver(this)
            isRegistered = false
        }
    }

    private companion object {
        private const val COVER_STATE = "state"
    }
}
