package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface
import ru.sberdevices.common.logger.Logger

/**
 * Фабрика, изолирующая имплементацию от потребителей [BinderHelper]
 * @param context application context
 * @param intent Intent с компонентом сервиса, к которому будем подключаться
 * @param logger внешний [Logger], если не передать, то будет создан логгер по умолчанию
 * @param getBinding вызывается в коллбеке onServiceConnected() [android.content.ServiceConnection].
 * Дает биндеру интерфейс сервиса
*/
@Deprecated(
    message = "Используйте BinderHelperFactory2 для создания инстанса BinderHelper. Он не требует impl модуля.",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith("BinderHelperFactory2")
)
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
    fun createCached(disconnectDelay: Long = DISCONNECT_DELAY): CachedBinderHelper<BinderInterface> =
        CachedBinderHelperImpl(create(), logger, disconnectDelay)
}

private const val DISCONNECT_DELAY = 3000L
