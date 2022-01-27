package ru.sberdevices.services.messaging.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageName(val type: MessageNameType) : Parcelable {
    @Parcelize
    enum class MessageNameType : Parcelable {
        CLOSE_APP,
        HEARTBEAT,
        RUN_APP,
        SERVER_ACTION,
        UPDATE_IP,
        GET_IHUB_TOKEN,
        RUN_APP_DEEPLINK
    }
}
