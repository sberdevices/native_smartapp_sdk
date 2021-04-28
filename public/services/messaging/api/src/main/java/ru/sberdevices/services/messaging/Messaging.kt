package ru.sberdevices.messaging

import androidx.annotation.AnyThread
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread

@WorkerThread
interface Messaging {

    /**
     * ServerActions from https://sbtatlas.sigma.sbrf.ru/wiki/pages/viewpage.action?pageId=2019165520
     * payload will be in server_action field
     *
     * simple ServerAction example
     * messageName: SERVER_ACTION
     * payload: {"actionId": "GET_STREAM", "parameters": {"content_id": "111111"}}
     *
     * runApp example
     * messageName: RUN_APP
     * payload: {"action_id": "run_app, "app_info": {"projectId":"5633938a-5ff3-49c9-ba7d-fe2a9944de78"}, "parameters": {}}
     *
     */
    fun sendAction(messageName: MessageName, payload: Payload): MessageId

    /**
     * Послать текст [text], как если бы запрос произнес пользователь.
     */
    fun sendText(text: String)

    /**
     * Добавить [listener] слушателя сообщений.
     */
    @AnyThread
    fun addListener(listener: Listener)

    /**
     * Удалить [listener] слушателя сообщений.
     */
    @AnyThread
    fun removeListener(listener: Listener)

    /**
     * Отключиться от сервиса и очистить ресурсы.
     */
    @AnyThread
    fun dispose()

    /**
     * Слушатель сообщений с бекенда.
     */
    @AnyThread
    interface Listener {
        /**
         * Новое сообщение с идентификатором [messageId] и данными [payload]
         */
        fun onMessage(messageId: MessageId, payload: Payload)

        /**
         * Пришла ошибка с бекенда [throwable] для сообщения [messageId]
         */
        fun onError(messageId: MessageId, throwable: Throwable)
    }
}

/**
 * Полезная нагрузка [data]
 */
data class Payload(val data: String)

/**
 * Идентификатор сообщения [value]
 */
data class MessageId(val value: String)

enum class MessageName {
    /**
     * Для запроса внутри приложения.
     */
    SERVER_ACTION,

    /**
     * Для запуска другого приложения.
     */
    RUN_APP,

    /**
     * Для отправки событий статистики.
     */
    HEARTBEAT,

    /**
     * Обновить IP
     */
    @RequiresPermission("ru.sberdevices.permission.IP_UPDATE")
    UPDATE_IP
}
