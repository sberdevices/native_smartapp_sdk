package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface
import ru.sberdevices.common.logger.Logger

/**
 * Фабрика, изолирующая имплементацию от потребителей [BinderHelper]
 * [context] application context
 * [intent] Intent с компонентом сервиса, к которому будем подключаться
 * [logger] внешний [Logger], если не передать, то будет создан логгер по умолчанию
 * [onDisconnect] вызывается в коллбеке onServiceDisconnected() [android.content.ServiceConnection]
 * [onBindingDied] вызывается в коллбеке onBindingDied() [android.content.ServiceConnection]
 * [onNullBinding] вызывается в коллбеке onNullBinding() [android.content.ServiceConnection]
 * [getBinding] вызывается в коллбеке onServiceConnected() [android.content.ServiceConnection]. Дает биндеру интерфейс сервиса
*/
class BinderHelperFactory<BinderInterface : IInterface>(
    private val context: Context,
    private val intent: Intent,
    logger: Logger? = null,
    private val getBinding: (IBinder) -> BinderInterface,
) {

    private val logger: Logger = logger ?: Logger.get(tag = "BinderHelper")

    fun create(): BinderHelper<BinderInterface> = BinderHelperImpl(
        context = context,
        intent = intent,
        logger = logger,
        getBinding = getBinding
    )

    /**
     * Создает [BinderHelper] поддерживающий кеширование соединения
     *
     * Внутри хранит счетчик соединений, connect инкрементирует счетчик, disconnect декрементирует
     * Реальное соединения и рассоединение происходит, когда счетчик = 0
     *
     * Каждый вызов [BinderHelper.execute] внутри себя вызывает connect/disconnect
     * При этом если реальное соединение уже существует, то физического connect/disconnect не будет
     *
     * Реальный disconnect происходит с задержкой [disconnectDelay].
     * Это позволяет последовательно вызывать несколько [BinderHelper.execute] в рамках одного физического соединения
     */
    fun createCached(disconnectDelay: Long = DISCONNECT_DELAY): BinderHelper<BinderInterface> =
        CachedBinderHelper(create(), logger, disconnectDelay)
}

private const val DISCONNECT_DELAY = 3000L
