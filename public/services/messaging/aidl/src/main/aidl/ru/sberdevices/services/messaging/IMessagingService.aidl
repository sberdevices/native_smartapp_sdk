// IMessagingService.aidl
package ru.sberdevices.services.messaging;

import ru.sberdevices.services.messaging.IMessagingListener;
import ru.sberdevices.services.messaging.model.MessageName;

interface IMessagingService {
    const int VERSION = 1;

    String sendAction(in MessageName messageName, String payload) = 10;
    String sendActionWithAppID(in MessageName messageName, String payload, String androidApplicationID) = 13;

    void sendText(String text) = 20;

    void addListener(in IMessagingListener listener) = 110;
    void removeListener(in IMessagingListener listener) = 120;
}
