package ru.sberdevices.services.paylib

import ru.sberdevices.services.paylib.entities.PayResultCode
import ru.sberdevices.services.paylib.codes.PayResultCode as AidlPayResultCode

/**
 * @author Николай Пахомов on 23.02.2022
 */
internal object PayResultCodeFactory {

    fun fromInt(resultCode: Int): PayResultCode = when (resultCode) {
        AidlPayResultCode.SUCCESS.rawCode -> PayResultCode.SUCCESS
        AidlPayResultCode.ERROR.rawCode -> PayResultCode.ERROR
        AidlPayResultCode.CANCELLED.rawCode -> PayResultCode.CANCELLED
        else -> PayResultCode.UNKNOWN
    }
}
