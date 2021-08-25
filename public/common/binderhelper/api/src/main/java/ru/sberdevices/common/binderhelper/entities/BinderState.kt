package ru.sberdevices.common.binderhelper.entities

import android.app.Service
import android.content.ServiceConnection

/**
 * Состояния соединения, соответствующие колбэкам класса [ServiceConnection].
 * @author Николай Пахомов on 08.07.2021
 */
enum class BinderState {
    /**
     * [ServiceConnection.onServiceConnected]
     *
     * Соединение с сервисом было установлено.
     */
    CONNECTED,
    /**
     * [ServiceConnection.onServiceDisconnected]
     *
     * Соединение с сервисом было потеряно. Обычно это означает падение процесса, в котором был запущен сервис.
     */
    DISCONNECTED,
    /**
     * [ServiceConnection.onBindingDied]
     *
     * Соединение потеряно. Ивенты больше приходить не будут. Для их получения нужен реконнект.
     */
    BINDING_DIED,
    /**
     * [ServiceConnection.onNullBinding]
     *
     * Сервис, в которому произошло подключение вернул null в [Service.onBind].
     */
    NULL_BINDING
}
