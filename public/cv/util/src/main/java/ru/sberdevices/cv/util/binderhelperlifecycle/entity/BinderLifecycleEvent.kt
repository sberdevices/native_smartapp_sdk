package ru.sberdevices.cv.util.binderhelperlifecycle.entity

import ru.sberdevices.cv.util.binderhelperlifecycle.EventPublisher.Event

/**
 * Events, matching [android.content.ServiceConnection] callbacks
 */
enum class BinderLifecycleEvent : Event {
    /**
     * ServiceConnection.onServiceConnected()
     * Connection to the Service has been established.
     */
    CONNECTED,
    /**
     * ServiceConnection.onServiceDisconnected()
     * Connection to the Service has been lost. This typically happens when the process hosting
     * the service has crashed or been killed.
     */
    DISCONNECTED,
    /**
     * ServiceConnection.onBindingDied()
     * The binding to connection is dead
     */
    BINDING_DIED,
    /**
     * ServiceConnection.onNullBinding()
     * Service being bound has returned null from its onBind() method.
     */
    NULL_BINDING
}
