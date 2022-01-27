package ru.sberdevices.common.logger

interface LoggerDelegate {

    fun verbose(tag: String, message: () -> String)

    fun verbose(tag: String, message: () -> String, throwable: Throwable)

    fun debug(tag: String, message: () -> String)

    fun debug(tag: String, message: () -> String, throwable: Throwable)

    fun info(tag: String, message: () -> String)

    fun info(tag: String, message: () -> String, throwable: Throwable)

    fun warn(tag: String, message: () -> String)

    fun warn(tag: String, message: () -> String, throwable: Throwable)

    fun error(tag: String, message: () -> String)

    fun error(tag: String, message: () -> String, throwable: Throwable)

    fun sensitive(tag: String, message: () -> String)
}
