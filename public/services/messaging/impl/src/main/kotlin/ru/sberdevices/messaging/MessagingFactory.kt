package ru.sberdevices.messaging

import android.content.Context
import androidx.annotation.AnyThread

object MessagingFactory {

    @AnyThread
    @JvmStatic
    fun create(appContext: Context): Messaging {
        return MessagingImpl(appContext.applicationContext)
    }
}
