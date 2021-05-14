package ru.sberdevices.cv.detection.entity

private const val MIRROR_CODE: Byte = 11

object DetectableMirror : Detectable {
    override val code: Byte = MIRROR_CODE
}
