package ru.sberdevices.cv.detection.entity.humans

data class Point(
    val relativeX: Float,
    val relativeY: Float,
    val confidence: Float?
) {
    fun getAbsoluteX(widthPx: Int): Int {
        return (widthPx * relativeX).toInt()
    }

    fun getAbsoluteY(heightPx: Int): Int {
        return (heightPx * relativeY).toInt()
    }
}
