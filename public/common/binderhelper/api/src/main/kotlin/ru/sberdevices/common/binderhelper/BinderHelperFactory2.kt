package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface

/**
 * Фабрика, изолирующая имплементацию от потребителей [BinderHelper]
 * @author Николай Пахомов on 24.11.2021
 */
interface BinderHelperFactory2 {
    /**
     * @param context Application context.
     * @param intent Intent с компонентом сервиса, к которому будем подключаться.
     * @param loggerTag тэг для логгира, если не передать, то будет создан логгер по умолчанию.
     * @param getBinding вызывается в коллбеке onServiceConnected() [android.content.ServiceConnection].
     * Дает биндеру интерфейс сервиса
     */
    fun <BinderInterface : IInterface> create(
        context: Context,
        intent: Intent,
        loggerTag: String? = null,
        getBinding: (IBinder) -> BinderInterface,
    ): BinderHelper<BinderInterface>

    /**
     * Создать закешированную версию [BinderHelper]. Используется для непродолжительных и частых IPC вызовов.
     * @param context Application context.
     * @param intent Intent с компонентом сервиса, к которому будем подключаться.
     * @param loggerTag тэг для логгира, если не передать, то будет создан логгер по умолчанию.
     * @param disconnectDelay таймаут на отключение от удаленного сервиса. По умолчанию - 3 секунды.
     * @param getBinding вызывается в коллбеке onServiceConnected() [android.content.ServiceConnection].
     * Дает биндеру интерфейс сервиса
     */
    fun <BinderInterface : IInterface> createCached(
        context: Context,
        intent: Intent,
        loggerTag: String?,
        disconnectDelay: Long = 3000L,
        getBinding: (IBinder) -> BinderInterface,
    ): CachedBinderHelper<BinderInterface>
}

/**
 * @see BinderHelperFactory2.create
 */
inline fun <reified BinderInterface : IInterface> BinderHelperFactory2.create(
    context: Context,
    intent: Intent,
    noinline getBinding: (IBinder) -> BinderInterface,
): BinderHelper<BinderInterface> = create(context, intent, BinderInterface::class.java.simpleName, getBinding)

/**
 * @see BinderHelperFactory2.createCached
 */
inline fun <reified BinderInterface : IInterface> BinderHelperFactory2.createCached(
    context: Context,
    intent: Intent,
    disconnectDelay: Long = 3000L,
    noinline getBinding: (IBinder) -> BinderInterface,
): CachedBinderHelper<BinderInterface> = createCached(
    context, intent, BinderInterface::class.java.simpleName, disconnectDelay, getBinding
)
