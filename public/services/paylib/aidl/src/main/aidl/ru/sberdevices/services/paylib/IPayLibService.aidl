package ru.sberdevices.services.paylib;

import ru.sberdevices.services.paylib.IPayStatusListener;

interface IPayLibService {
    const String PLATFORM_VERSION = "1.79.0";

    boolean launchPayDialog(in String invoiceId) = 10;

    void addPayStatusListener(in IPayStatusListener listener) = 120;
    void removePayStatusListener(in IPayStatusListener listener) = 121;
}
