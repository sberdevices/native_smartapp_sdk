package ru.sberdevices.common.logger

import android.util.Log

class AndroidLoggerDelegate(
    private val allowLogSensitive: Boolean = false
) : LoggerDelegate {

    override fun verbose(tag: String, message: () -> String) {
        Log.v(tag, message.invoke())
    }

    override fun verbose(tag: String, message: () -> String, throwable: Throwable) {
        Log.v(tag, message.invoke(), throwable)
    }

    override fun debug(tag: String, message: () -> String) {
        Log.d(tag, message.invoke())
    }

    override fun debug(tag: String, message: () -> String, throwable: Throwable) {
        Log.d(tag, message.invoke(), throwable)
    }

    override fun info(tag: String, message: () -> String) {
        Log.i(tag, message.invoke())
    }

    override fun info(tag: String, message: () -> String, throwable: Throwable) {
        Log.i(tag, message.invoke(), throwable)
    }

    override fun warn(tag: String, message: () -> String) {
        Log.w(tag, message.invoke())
    }

    override fun warn(tag: String, message: () -> String, throwable: Throwable) {
        Log.w(tag, message.invoke(), throwable)
    }

    override fun error(tag: String, message: () -> String) {
        Log.e(tag, message.invoke())
    }

    override fun error(tag: String, message: () -> String, throwable: Throwable) {
        Log.e(tag, message.invoke(), throwable)
    }

    /**
     * Caution!
     */
    override fun sensitive(tag: String, message: () -> String) {
        if (allowLogSensitive) {
            Log.w(tag, message.invoke())
        }
    }

    // TODO add sensitive(tag: String, message: String, throwable: Throwable)
}
