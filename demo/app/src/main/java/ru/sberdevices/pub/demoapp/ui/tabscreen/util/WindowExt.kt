package ru.sberdevices.pub.demoapp.ui.tabscreen.util

import android.view.View
import android.view.Window
import ru.sberdevices.common.logger.Logger

private val logger by lazy { Logger.get("WindowExt") }

/**
 * https://developer.android.com/training/system-ui/immersive
 */
fun Window.enterImmersiveMode() {
    logger.debug { "enterImmersiveMode()" }
    val currentFlags = decorView.systemUiVisibility
    decorView.systemUiVisibility = currentFlags or View.SYSTEM_UI_FLAG_IMMERSIVE
}

fun Window.exitImmersiveMode() {
    logger.debug { "exitImmersiveMode()" }
    val currentFlags = decorView.systemUiVisibility
    decorView.systemUiVisibility = currentFlags and View.SYSTEM_UI_FLAG_IMMERSIVE.inv()
}


