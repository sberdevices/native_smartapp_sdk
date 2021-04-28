package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.os.IBinder

/**
 * Фабрика, изолирующая имплементацию от потребителей [BinderHelper2]
 *
 * @author Ирина Карпенко on 24.03.2021
 */
object BinderHelper2Factory {
    /**
     * @param context application context
     * @param intent Intent с компонентом сервиса, к которому будем подключаться
     * @param onDisconnect вызывается в коллбеке onServiceDisconnected() [android.content.ServiceConnection]
     * @param onBindingDied вызывается в коллбеке onBindingDied() [android.content.ServiceConnection]
     * @param onNullBinding вызывается в коллбеке onNullBinding() [android.content.ServiceConnection]
     * @param getBinding вызывается в коллбеке onServiceConnected() [android.content.ServiceConnection]. Дает биндеру интерфейс сервиса
     */
    fun <BinderInterface : Any> getBinderHelper2(
        context: Context,
        intent: Intent,
        onDisconnect: () -> Unit = {},
        onBindingDied: () -> Unit = {},
        onNullBinding: () -> Unit = {},
        getBinding: (IBinder) -> BinderInterface,
    ): BinderHelper2<BinderInterface> {
        return BinderHelper2Impl(
            context = context,
            intent = intent,
            onDisconnect = onDisconnect,
            onBindingDied = onBindingDied,
            onNullBinding = onNullBinding,
            getBinding = getBinding
        )
    }
}
