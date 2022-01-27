package ru.sberdevices.messaging

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.sberdevices.common.binderhelper.BinderHelperFactory
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.messaging.IMessagingListener
import ru.sberdevices.services.messaging.IMessagingService
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import ru.sberdevices.services.messaging.model.MessageName as MessageNameModel

private const val SERVICE_APP_ID = "ru.sberdevices.services"
private const val SERVICE_NAME = "ru.sberdevices.services.messaging.MessagingService"

private val BIND_INTENT = Intent().apply {
    component = ComponentName(SERVICE_APP_ID, SERVICE_NAME)
}

@WorkerThread
internal class MessagingImpl @AnyThread constructor(
    context: Context
) : Messaging {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val logger = Logger.get("MessagingImpl")
    private val listeners = ConcurrentLinkedQueue<Messaging.Listener>()

    private val messageListener = object : IMessagingListener.Stub() {
        override fun onMessage(messageId: String?, payload: String?) {
            logger.debug { "onMessage: $messageId" }
            if (messageId != null && payload != null) {
                listeners.forEach { it.onMessage(MessageId(messageId), Payload(payload)) }
            } else {
                logger.error { "null value onMessage messageId=$messageId payload=$payload" }
            }
        }

        override fun onError(messageId: String?, error: String?) {
            logger.debug { "onError: $messageId" }
            if (messageId != null && error != null) {
                listeners.forEach { it.onError(MessageId(messageId), IOException(error)) }
            } else {
                logger.error { "null value onError messageId=$messageId error=$error" }
            }
        }

        override fun onNavigationCommand(payload: String) {
            logger.debug { "onNavigationCommand" }
            listeners.forEach { it.onNavigationCommand(Payload(payload)) }
        }
    }

    private val helper = BinderHelperFactory(context.applicationContext, BIND_INTENT) {
        IMessagingService.Stub.asInterface(it)
    }.create()

    init {
        helper.connect()
        scope.launch {
            helper.execute {
                logger.debug { "registerIMessagingListener()" }
                it.addListener(messageListener)
            }
        }
    }

    override fun sendAction(messageName: MessageName, payload: Payload): MessageId {
        logger.debug { "sendAction() with messageName" }
        require(Looper.myLooper() != Looper.getMainLooper())
        val id: String = runBlocking {
            helper.execute { service ->
                service.sendAction(
                    MessageNameModel(messageName.convertToType()),
                    payload.data
                )
            }
        }!!
        return MessageId(id)
    }

    @RequiresPermission("ru.sberdevices.permission.CROSS_APP_ACTION")
    override fun sendAction(messageName: MessageName, payload: Payload, androidApplicationID: String): MessageId {
        logger.debug { "sendAction() with messageName and androidApplicationID" }
        require(Looper.myLooper() != Looper.getMainLooper())
        val id: String = runBlocking {
            helper.execute { service ->
                service.sendActionWithAppID(
                    MessageNameModel(messageName.convertToType()),
                    payload.data,
                    androidApplicationID
                )
            }
        }!!
        return MessageId(id)
    }

    override fun sendText(text: String) {
        logger.debug { "sending text $text" }
        scope.launch { helper.execute { service -> service.sendText(text) } }
    }

    @AnyThread
    override fun addListener(listener: Messaging.Listener) {
        logger.debug { "addListener: $listener" }
        listeners.add(listener)
    }

    @AnyThread
    override fun removeListener(listener: Messaging.Listener) {
        logger.debug { "removeListener: $listener" }
        listeners.remove(listener)
    }

    @AnyThread
    override fun dispose() {
        logger.info { "dispose()" }
        listeners.clear()
        helper.disconnect()
        scope.cancel()
    }

    private fun MessageName.convertToType(): MessageNameModel.MessageNameType {
        return when (this) {
            MessageName.SERVER_ACTION -> MessageNameModel.MessageNameType.SERVER_ACTION
            MessageName.RUN_APP -> MessageNameModel.MessageNameType.RUN_APP
            MessageName.UPDATE_IP -> MessageNameModel.MessageNameType.UPDATE_IP
            MessageName.HEARTBEAT -> MessageNameModel.MessageNameType.HEARTBEAT
            MessageName.CLOSE_APP -> MessageNameModel.MessageNameType.CLOSE_APP
            MessageName.GET_IHUB_TOKEN -> MessageNameModel.MessageNameType.GET_IHUB_TOKEN
            MessageName.RUN_APP_DEEPLINK -> MessageNameModel.MessageNameType.RUN_APP_DEEPLINK
        }
    }
}
