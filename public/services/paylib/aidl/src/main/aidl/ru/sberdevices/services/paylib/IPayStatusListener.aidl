package ru.sberdevices.services.paylib;

//import ru.sberdevices.services.paylib.PayStatus;

interface IPayStatusListener {
   oneway void onPayStatusUpdated(in String invoiceId, in int resultCode) = 10;
}
