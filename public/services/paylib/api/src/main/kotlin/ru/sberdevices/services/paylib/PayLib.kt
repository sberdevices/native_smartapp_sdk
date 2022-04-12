package ru.sberdevices.services.paylib

import ru.sberdevices.services.paylib.entities.PayStatus

interface PayLib {

    suspend fun launchPayDialog(invoiceId: String): Result<PayStatus>
}
