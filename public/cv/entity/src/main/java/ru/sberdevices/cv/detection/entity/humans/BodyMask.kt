package ru.sberdevices.cv.detection.entity.humans

import ru.sberdevices.cv.entity.HasFrameTime

data class BodyMask(
    val data: ByteArray,
    val rowCount: Int,
    val columnCount: Int,
    override val frameTimestampMs: Long
) : HasFrameTime {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BodyMask

        if (!data.contentEquals(other.data)) return false
        if (rowCount != other.rowCount) return false
        if (columnCount != other.columnCount) return false
        if (frameTimestampMs != other.frameTimestampMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + rowCount
        result = 31 * result + columnCount
        result = 31 * result + frameTimestampMs.hashCode()
        return result
    }
}
