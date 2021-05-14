package ru.sberdevices.cv.detection.entity.gesture

import ru.sberdevices.cv.entity.HasFrameTime

data class Gesture(
    val type: Type,
    val metadata: String?,
    override val frameTimestampMs: Long
) : HasFrameTime {

    enum class Type(val code: Byte) {
        NONE(code = 0),
        PALM(code = 5),
        THUMB_UP(code = 7),
        THUMB_DOWN(code = 8),
        FINGER_TO_LIPS(code = 9),
        OK(code = 10);

        init {
            require(code >= 0) { "gesture type code must be >= 0" }
        }

        companion object {
            fun fromCode(code: Byte): Type {
                return requireNotNull(values().find { it.code == code }) { "no gesture type for code $code" }
            }
        }
    }
}
