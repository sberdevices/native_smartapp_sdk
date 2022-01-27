// IMessagingListener.aidl
package ru.sberdevices.services.messaging;

interface IMessagingListener {
    oneway void onMessage(String messageId, String payload) = 10;
    oneway void onError(String messageId, String error) = 20;
    oneway void onNavigationCommand(String payload) = 30;
}
