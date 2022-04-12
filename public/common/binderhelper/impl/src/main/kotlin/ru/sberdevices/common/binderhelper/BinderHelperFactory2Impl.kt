package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface
import ru.sberdevices.common.logger.Logger

/**
 * Фабрика, изолирующая имплементацию от потребителей [BinderHelper]
 */
class BinderHelperFactory2Impl : BinderHelperFactory2 {

    /**
     * @param context application context
     * @param intent Intent с компонентом сервиса, к которому будем подключаться
     * @param loggerTag тэг для логгирования, если не передать, то будет использоваться `BinderHelper`
     * @param getBinding лямбда, которая должна вернуть тип сервиса.
     */
    override fun <BinderInterface : IInterface> create(
        context: Context,
        intent: Intent,
        loggerTag: String?,
        getBinding: (IBinder) -> BinderInterface,
    ): BinderHelper<BinderInterface> = BinderHelperImpl(
        context = context,
        intent = intent,
        logger = Logger.get(loggerTag ?: "BinderHelper"),
        getBinding = getBinding
    )

    /**
     * Создает [CachedBinderHelper] поддерживающий кеширование соединения
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
    override fun <BinderInterface : IInterface> createCached(
        context: Context,
        intent: Intent,
        loggerTag: String?,
        disconnectDelay: Long,
        getBinding: (IBinder) -> BinderInterface,
    ): CachedBinderHelper<BinderInterface> {
        return CachedBinderHelperImpl(
            create(context, intent, loggerTag, getBinding),
            Logger.get(loggerTag ?: "BinderHelper"),
            disconnectDelay
        )
    }

}
