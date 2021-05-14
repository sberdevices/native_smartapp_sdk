package ru.sberdevices.cv.detection.entity.humans

import ru.sberdevices.cv.entity.BoundingBox
import ru.sberdevices.cv.entity.HasFrameTime

data class BoundingBoxes(
    val boundingBoxes: List<BoundingBox>,
    override val frameTimestampMs: Long
) : HasFrameTime
