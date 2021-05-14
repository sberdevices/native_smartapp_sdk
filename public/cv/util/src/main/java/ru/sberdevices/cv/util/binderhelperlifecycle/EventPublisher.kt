package ru.sberdevices.cv.util.binderhelperlifecycle

import kotlinx.coroutines.flow.Flow

internal interface EventPublisher<Event : EventPublisher.Event> {
    val events: Flow<Event>

    interface Event
}
