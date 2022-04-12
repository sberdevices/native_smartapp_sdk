package ru.sberdevices.common.binderhelper.entities

/**
 * Исключения, которые могут кинуться во время вызовов Remote методов.
 * @author Николай Пахомов on 02.02.2022
 */
sealed class BinderException(override val message: String) : Exception(message) {
    /**
     * IPC соединение не было установлено.
     */
    class ConnectionNotEstablished : BinderException("Binder connection hasn't been established")

    /**
     * Корутина, в которой ожидалось выполнение Remote метода была отменена
     */
    class CoroutineContextCancelled : BinderException("Coroutine with connection was cancelled")

    /**
     * Remote метод вернул null значение, хотя этого не ожидалось.
     * Скорей всего текущая версия SDK обращается к старому SPS, который не умеет обрабатывать эти методы.
     */
    class ReceivedNullValue : BinderException(
        "Received null value from IPC call, expected non-null. Current SDK version might not be yet supported"
    )
}
