package ru.sberdevices.common.logger

import android.util.Log
import ru.sberdevices.common.logger.mode.LogLevel

class AndroidLoggerDelegate(
    private val allowLogSensitive: Boolean = false,
    private val isDebugBuild: Boolean = BuildConfig.DEBUG,
    private inline val logLevel: () -> LogLevel = { LogLevel.VERBOSE },
    private val prefix: String = "",
) : LoggerDelegate {

    override fun verbose(tag: String, message: () -> String) {
        callLoggerFn(minLogLevel = LogLevel.VERBOSE) { Log.v("$prefix$tag", message.invoke()) }
    }

    override fun verbose(tag: String, message: () -> String, throwable: Throwable) {
        callLoggerFn(minLogLevel = LogLevel.VERBOSE) { Log.v("$prefix$tag", message.invoke(), throwable) }
    }

    override fun debug(tag: String, message: () -> String) {
        callLoggerFn(minLogLevel = LogLevel.DEBUG) { Log.d("$prefix$tag", message.invoke()) }
    }

    override fun debug(tag: String, message: () -> String, throwable: Throwable) {
        callLoggerFn(minLogLevel = LogLevel.DEBUG) { Log.d("$prefix$tag", message.invoke(), throwable) }
    }

    override fun info(tag: String, message: () -> String) {
        callLoggerFn(minLogLevel = LogLevel.INFO) { Log.i("$prefix$tag", message.invoke()) }
    }

    override fun info(tag: String, message: () -> String, throwable: Throwable) {
        callLoggerFn(minLogLevel = LogLevel.INFO) { Log.i("$prefix$tag", message.invoke(), throwable) }
    }

    override fun warn(tag: String, message: () -> String) {
        callLoggerFn(minLogLevel = LogLevel.WARN) { Log.w("$prefix$tag", message.invoke()) }
    }

    override fun warn(tag: String, message: () -> String, throwable: Throwable) {
        callLoggerFn(minLogLevel = LogLevel.WARN) { Log.w("$prefix$tag", message.invoke(), throwable) }
    }

    override fun error(tag: String, message: () -> String) {
        callLoggerFn(minLogLevel = LogLevel.ERROR) { Log.e("$prefix$tag", message.invoke()) }
    }

    override fun error(tag: String, message: () -> String, throwable: Throwable) {
        callLoggerFn(minLogLevel = LogLevel.ERROR) { Log.e("$prefix$tag", message.invoke(), throwable) }
    }

    /**
     * Caution!
     */
    override fun sensitive(tag: String, message: () -> String) {
        if (allowLogSensitive) {
            callLoggerFn(minLogLevel = LogLevel.WARN) { Log.w("$prefix$tag", message.invoke()) }
        }
    }

    private inline fun callLoggerFn(minLogLevel: LogLevel, loggerFn: () -> Unit) {
        if (isDebugBuild) {
            loggerFn.invoke()
            return
        }

        if (minLogLevel.weight >= logLevel.invoke().weight) {
            loggerFn.invoke()
        }
    }
}
