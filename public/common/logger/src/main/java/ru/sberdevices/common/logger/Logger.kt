package ru.sberdevices.common.logger

import android.webkit.ConsoleMessage
import androidx.annotation.AnyThread
import ru.sberdevices.common.assert.Asserts

@AnyThread
class Logger private constructor(
    private val tag: String,
    private val delegates: List<LoggerDelegate>
) {

    fun verbose(message: () -> String) {
        delegates.forEach { it.verbose(tag, message) }
    }

    fun verbose(throwable: Throwable, message: () -> String) {
        delegates.forEach { it.verbose(tag, message, throwable) }
    }

    fun debug(message: () -> String) {
        delegates.forEach { it.debug(tag, message) }
    }

    fun debug(throwable: Throwable, message: () -> String) {
        delegates.forEach { it.debug(tag, message, throwable) }
    }

    fun info(message: () -> String) {
        delegates.forEach { it.info(tag, message) }
    }

    fun info(throwable: Throwable, message: () -> String) {
        delegates.forEach { it.info(tag, message, throwable) }
    }

    fun warn(message: () -> String) {
        delegates.forEach { it.warn(tag, message) }
    }

    fun warn(throwable: Throwable, message: () -> String) {
        delegates.forEach { it.warn(tag, message, throwable) }
    }

    fun error(message: () -> String) {
        delegates.forEach { it.error(tag, message) }
    }

    fun error(throwable: Throwable, message: () -> String) {
        delegates.forEach { it.error(tag, message, throwable) }
    }

    fun sensitive(message: () -> String) {
        delegates.forEach { it.warn(tag, message) }
    }

    /**
     * Метод для логирования js-ных логов
     */
    fun consoleLog(consoleMessage: ConsoleMessage) {
        val message = "[CONSOLE_JS]: ${consoleMessage.message()}"
        when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.ERROR -> error { message }
            ConsoleMessage.MessageLevel.WARNING -> warn { message }
            else -> debug { message }
        }
    }

    // TODO add sensitive(throwable: Throwable, message: () -> String) {

    companion object {

        @Volatile
        private var delegates: List<LoggerDelegate> = listOf()

        fun get(tag: String): Logger {
            Asserts.assertTrue(delegates.isNotEmpty())
            return Logger(tag, delegates)
        }

        fun lazy(tag: String) = lazy { get(tag) }

        @Deprecated("please to use another variant. @See setDelegates(vararg delegates: LoggerDelegate)")
        fun setDelegates(delegates: List<LoggerDelegate>) {
            if (delegates.isEmpty()) {
                Asserts.fail("")
            }

            Companion.delegates = delegates.toList()
        }

        @AnyThread
        @JvmStatic
        fun setDelegates(vararg delegates: LoggerDelegate) {
            if (delegates.isEmpty()) {
                Asserts.fail("delegates is empty!")
            }

            Companion.delegates = delegates.toList()
        }
    }
}
