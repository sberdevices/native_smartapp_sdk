package ru.sberdevices.common.logger

import android.webkit.ConsoleMessage
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
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
        delegates.forEach { it.sensitive(tag, message) }
    }

    /**
     * Метод для логирования js-ных логов
     */
    fun consoleLog(consoleMessage: ConsoleMessage) {
        val message = "[CONSOLE_JS]: ${consoleMessage.message()}"
        when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.ERROR -> error { message }
            ConsoleMessage.MessageLevel.WARNING -> warn { message }
            ConsoleMessage.MessageLevel.DEBUG -> debug { message }
            else -> info { message }
        }
    }

    companion object {

        @Volatile
        private var delegates: List<LoggerDelegate> = listOf()

        fun get(tag: String): Logger {
            Asserts.assertTrue(delegates.isNotEmpty())
            return Logger(tag, delegates)
        }

        fun lazy(tag: String) = lazy { get(tag) }

        @Deprecated("Тэг класса нужно указывать через String", replaceWith = ReplaceWith("Logger.get(T)"))
        inline fun <reified T> get() = get(T::class.java.simpleName)

        @Deprecated("Тэг класса нужно указывать через String", replaceWith = ReplaceWith("Logger.lazy(T)"))
        inline fun <reified T> lazy() = lazy { get(T::class.java.simpleName) }

        @MainThread
        @JvmStatic
        fun setDelegates(vararg delegates: LoggerDelegate) {
            setDelegates(delegates.toList())
        }

        @MainThread
        @JvmStatic
        fun setDelegates(delegates: List<LoggerDelegate>) {
            if (delegates.isEmpty()) {
                Asserts.fail("delegates is empty!")
            }

            Companion.delegates = delegates
        }
    }
}
