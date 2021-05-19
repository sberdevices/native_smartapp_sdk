package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface

/**
 * Фабрика, изолирующая имплементацию от потребителей [BinderHelper].
 *
 * [context] application context
 * [intent] Intent с компонентом сервиса, к которому будем подключаться
 * [onDisconnect] вызывается в коллбеке onServiceDisconnected() [android.content.ServiceConnection]
 * [onBindingDied] вызывается в коллбеке onBindingDied() [android.content.ServiceConnection]
 * [onNullBinding] вызывается в коллбеке onNullBinding() [android.content.ServiceConnection]
 * [getBinding] вызывается в коллбеке onServiceConnected() [android.content.ServiceConnection]. Дает биндеру интерфейс сервиса
 */
class BinderHelperFactory<BinderInterface : IInterface>(
    private val context: Context,
    private val intent: Intent,
    private val onDisconnect: () -> Unit = {},
    private val onBindingDied: () -> Unit = {},
    private val onNullBinding: () -> Unit = {},
    private val getBinding: (IBinder) -> BinderInterface,
) {

    fun create(): BinderHelper<BinderInterface> {
        return BinderHelperImpl(
            context = context,
            intent = intent,
            onDisconnect = onDisconnect,
            onBindingDied = onBindingDied,
            onNullBinding = onNullBinding,
            getBinding = getBinding
        )
    }
}
