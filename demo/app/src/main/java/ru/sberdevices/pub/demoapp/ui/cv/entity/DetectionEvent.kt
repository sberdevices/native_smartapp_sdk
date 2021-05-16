package ru.sberdevices.pub.demoapp.ui.cv.entity

import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.Humans

internal sealed class DetectionEvent

internal data class GestureDetectionEvent(val gesture: Gesture) : DetectionEvent()
internal data class HumansDetectionEvent(val humans: Humans) : DetectionEvent()
