package ru.sberdevices.cv.detection.entity

/**
 * Interface, denoting type as detection kind
 */
interface Detectable {
    val code: Byte

    @Suppress("EmptyClassBlock") // Empty companion used for static extension
    companion object {}
}
