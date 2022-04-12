package ru.sberdevices.services.paylib.entities

/**
 * Результат оплаты
 * @param invoiceId Идентификатор счета
 * @param resultCode Результат завершения оплаты
 *
 * @author Николай Пахомов on 23.02.2022
 */
data class PayStatus(
    val invoiceId: String,
    val resultCode: PayResultCode,
)
