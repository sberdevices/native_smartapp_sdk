package ru.sberdevices.common.logger.mode

/**
 * Минимальный активный уровень логгирования приложения.
 */
enum class LogLevel(val weight: Int) {
    /**
     * Включено verbose логгирование.
     */
    VERBOSE(1),
    /**
     * Включено debug логгирование.
     */
    DEBUG(2),
    /**
     * Включено info логгирование.
     */
    INFO(3),
    /**
     * Включено warn логгирование.
     */
    WARN(4),
    /**
     * Включено error логгирование.
     */
    ERROR(5),
    /**
     * Логгирование на устройстве отключено.
     */
    NONE(6)
}

fun String?.toLogLevel() = when (this) {
    "VERBOSE" -> LogLevel.VERBOSE
    "DEBUG" -> LogLevel.DEBUG
    "INFO" -> LogLevel.INFO
    "WARN" -> LogLevel.WARN
    "ERROR" -> LogLevel.ERROR
    "NONE" -> LogLevel.NONE
    else -> LogLevel.INFO
}
