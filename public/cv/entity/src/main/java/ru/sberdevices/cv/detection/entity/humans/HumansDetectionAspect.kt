package ru.sberdevices.cv.detection.entity.humans

import ru.sberdevices.cv.detection.entity.Detectable

private const val FACE_BOUNDING_BOX_CODE: Byte = 7
private const val FACE_LANDMARKS_CODE: Byte = 8
private const val BODY_BOUNDING_BOX_CODE: Byte = 9
private const val BODY_LANDMARKS_HOMA_NET_CODE: Byte = 3
private const val BODY_LANDMARKS_SENTAL_NET_CODE: Byte = 10
private const val BODY_SEGMENTATION_CODE: Byte = 6

sealed class HumansDetectionAspect(override val code: Byte) : Detectable {
    sealed class Face(override val code: Byte) : HumansDetectionAspect(code) {
        object BoundingBox : Face(FACE_BOUNDING_BOX_CODE)
        object Landmarks : Face(FACE_LANDMARKS_CODE)
    }

    sealed class Body(override val code: Byte) : HumansDetectionAspect(code) {
        object BoundingBox : Body(BODY_BOUNDING_BOX_CODE)
        sealed class Landmarks(override val code: Byte) : Body(code) {
            object HomaNet : Landmarks(BODY_LANDMARKS_HOMA_NET_CODE)
            object SentalNet : Landmarks(BODY_LANDMARKS_SENTAL_NET_CODE)
        }

        object Segmentation : Body(BODY_SEGMENTATION_CODE)
    }

    @Suppress("EmptyClassBlock") // Empty companion used for static extension
    companion object {}
}
