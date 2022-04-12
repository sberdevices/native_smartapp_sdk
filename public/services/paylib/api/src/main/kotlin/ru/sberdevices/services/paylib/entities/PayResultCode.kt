package ru.sberdevices.services.paylib.entities

/**
 * Результат завершения оплаты
 */
enum class PayResultCode {

    /**
     * Оплата завершилась успешно
     */
    SUCCESS,

    /**
     * Ошибка
     */
    ERROR,

    /**
     * Закрыто пользователем
     */
    CANCELLED,

    /**
     * Неподдерживаемый код
     */
    UNKNOWN,
}
