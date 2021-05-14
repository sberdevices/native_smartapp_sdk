package ru.sberdevices.cv.detection.entity.humans

import ru.sberdevices.cv.entity.HasFrameTime

data class Landmarks(
    val pointGroups: List<PointGroup>,
    override val frameTimestampMs: Long,
) : HasFrameTime
