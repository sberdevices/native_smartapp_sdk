package ru.sberdevices.services.paylib.aidl.wrappers

import kotlinx.coroutines.flow.SharedFlow
import ru.sberdevices.services.paylib.IPayStatusListener
import ru.sberdevices.services.paylib.entities.PayStatus

/**
 * @author Николай Пахомов on 23.02.2022
 */
internal abstract class PayStatusListenerWrapper : IPayStatusListener.Stub() {
    abstract val payStatusFlow: SharedFlow<PayStatus>
}
