package ru.sberdevices.services.paylib.aidl.wrappers

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.paylib.PayResultCodeFactory
import ru.sberdevices.services.paylib.entities.PayStatus

/**
 * @author Николай Пахомов on 23.02.2022
 */
internal class PayStatusListenerWrapperImpl : PayStatusListenerWrapper() {

    private val logger = Logger.get("PayStatusListenerWrapperImpl")

    private val mutableEventsFlow = MutableSharedFlow<PayStatus>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST, // Для tryEmit
        extraBufferCapacity = 16 // Для BufferOverflow.DROP_OLDEST
    )

    override val payStatusFlow: SharedFlow<PayStatus> = mutableEventsFlow

    override fun onPayStatusUpdated(invoiceId: String, resultCode: Int) {
        val payStatus = PayStatus(
            invoiceId = invoiceId,
            resultCode = PayResultCodeFactory.fromInt(resultCode),
        )
        val result = mutableEventsFlow.tryEmit(payStatus)
        logger.debug { "onPayStatusUpdated, emit result: $result" }
    }
}
