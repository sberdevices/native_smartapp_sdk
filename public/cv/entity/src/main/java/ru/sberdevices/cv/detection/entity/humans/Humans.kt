package ru.sberdevices.cv.detection.entity.humans

/**
 * The unified detection describing humans. Received content depends on humans detection aspects
 * requested from computer vision service
 */
data class Humans(
    val bodyBoundingBoxes: BoundingBoxes?,
    val faceBoundingBoxes: BoundingBoxes?,
    val faceTrackIds: List<Long>,
    val bodyLandmarks: Landmarks?,
    val faceLandmarks: Landmarks?,
    val bodyTrackIds: List<Long>,
    val bodyMask: BodyMask?
) {
    companion object {
        @JvmField
        val EMPTY = Humans(
            bodyBoundingBoxes = null,
            faceBoundingBoxes = null,
            faceTrackIds = emptyList(),
            bodyLandmarks = null,
            faceLandmarks = null,
            bodyTrackIds = emptyList(),
            bodyMask = null
        )
    }
}
