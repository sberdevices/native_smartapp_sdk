package ru.sberdevices.common.logger.mode

/**
 * Репозиторий актуального [LogLevel] для вашего приложения.
 * @author Николай Пахомов on 03.08.2021
 */
interface LogLevelRepository {
    /**
     * Получить текущий [LogLevel].
     *
     * Если ранее значение не было выставлено, то возмется [LogLevel.INFO]
     */
    fun getCurrentLogLevel(): LogLevel

    /**
     * Выставить текущее значение [LogLevel].
     *
     * Значение будет сохранено как и в памяти, так и на диск.
     */
    fun setCurrentLogLevel(value: LogLevel)
}
