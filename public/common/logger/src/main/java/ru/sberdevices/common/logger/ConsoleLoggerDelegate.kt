package ru.sberdevices.common.logger

import java.time.LocalDateTime

/**
 * Print to console. For using in tests
 */
class ConsoleLoggerDelegate(
    private val allowLogSensitive: Boolean = false
) : LoggerDelegate {
    private fun getTime() = LocalDateTime.now().toLocalTime()

    override fun verbose(tag: String, message: () -> String) = println("${getTime()} $tag ${message.invoke()}")

    override fun verbose(tag: String, message: () -> String, throwable: Throwable) = println("${getTime()} $tag ${message.invoke()}")

    override fun debug(tag: String, message: () -> String) = println("${getTime()} $tag ${message.invoke()}")

    override fun debug(tag: String, message: () -> String, throwable: Throwable) = println("${getTime()} $tag ${message.invoke()}")

    override fun info(tag: String, message: () -> String) = println("${getTime()} $tag ${message.invoke()}")

    override fun info(tag: String, message: () -> String, throwable: Throwable) = println("${getTime()} $tag ${message.invoke()}")

    override fun warn(tag: String, message: () -> String) = println("${getTime()} $tag ${message.invoke()}")

    override fun warn(tag: String, message: () -> String, throwable: Throwable) = println("${getTime()} $tag ${message.invoke()}")

    override fun error(tag: String, message: () -> String) = println("${getTime()} $tag ${message.invoke()}")

    override fun error(tag: String, message: () -> String, throwable: Throwable) = println("${getTime()} $tag ${message.invoke()}")

    /**
     * Caution!
     */
    override fun sensitive(tag: String, message: () -> String) {
        if (allowLogSensitive) {
            println("${getTime()} $tag ${message.invoke()}")
        }
    }
}
