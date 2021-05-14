package ru.sberdevices.cv.detection.entity.gesture

import ru.sberdevices.cv.detection.entity.Detectable

private const val GESTURE_CODE: Byte = 2

object DetectableGesture : Detectable {
    override val code: Byte = GESTURE_CODE
}
