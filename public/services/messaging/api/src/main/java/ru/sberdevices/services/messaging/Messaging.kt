package ru.sberdevices.messaging

import androidx.annotation.AnyThread
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread

@WorkerThread
interface Messaging {

    /**
     * simple ServerAction example
     * messageName: SERVER_ACTION
     * payload: {"action_id": "GET_STREAM", "parameters": {"content_id": "111111"}}
     *
     * runApp example
     * messageName: RUN_APP
     * payload: {"action_id": "run_app, "app_info": {"projectId":"5633938a-5ff3-49c9-ba7d-fe2a9944de78"}, "parameters": {}}
     *
     */
    fun sendAction(messageName: MessageName, payload: Payload): MessageId

    /**
     * Send server_action with source app androidApplicationID
     *
     * For inner use only
     * Requires permission: "ru.sberdevices.permission.CROSS_APP_ACTION"
     */
    @RequiresPermission("ru.sberdevices.permission.CROSS_APP_ACTION")
    fun sendAction(messageName: MessageName, payload: Payload, androidApplicationID: String): MessageId

    /**
     * Send text [text], as if this text was spoken by user.
     */
    fun sendText(text: String)

    /**
     * Add message [listener].
     */
    @AnyThread
    fun addListener(listener: Listener)

    /**
     * Remove message [listener].
     */
    @AnyThread
    fun removeListener(listener: Listener)

    /**
     * Disconnect from service and clear resources.
     */
    @AnyThread
    fun dispose()

    /**
     * Smartapp backend's messages listener.
     */
    @AnyThread
    interface Listener {
        /**
         * New message with id [messageId] and [payload].
         */
        fun onMessage(messageId: MessageId, payload: Payload)

        /**
         * Error from backend [throwable] for message with [messageId].
         */
        fun onError(messageId: MessageId, throwable: Throwable)
    }
}

/**
 * Some useful [data].
 */
data class Payload(val data: String)

/**
 * Message id [value].
 */
data class MessageId(val value: String)

enum class MessageName {
    /**
     * Request to your own backend.
     */
    SERVER_ACTION,

    /**
     * For opening up another app.
     */
    RUN_APP,

    /**
     * To send some statistics.
     */
    HEARTBEAT,

    /**
     * Update IP.
     */
    @RequiresPermission("ru.sberdevices.permission.IP_UPDATE")
    UPDATE_IP,

    /**
     * Close app.
     */
    CLOSE_APP
}
