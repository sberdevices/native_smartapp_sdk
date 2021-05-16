package ru.sberdevices.pub.demoapp.ui.cv.mapper

import androidx.annotation.DrawableRes
import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.HumansDetectionAspect
import ru.sberdevices.pub.demoapp.ui.cv.entity.Control
import ru.sberdevices.services.pub.demoapp.R

internal fun Control.toDomainHumansDetectionAspect(): HumansDetectionAspect? {
    return when (this) {
        Control.FACE_BOUNDING_BOX -> HumansDetectionAspect.Face.BoundingBox
        Control.FACE_LANDMARKS -> HumansDetectionAspect.Face.Landmarks
        Control.BODY_BOUNDING_BOX -> HumansDetectionAspect.Body.BoundingBox
        Control.BODY_LANDMARKS_HOMA_NET -> HumansDetectionAspect.Body.Landmarks.HomaNet
        Control.BODY_LANDMARKS_SENTAL_NET -> HumansDetectionAspect.Body.Landmarks.SentalNet
        Control.BODY_SEGMENTATION -> HumansDetectionAspect.Body.Segmentation
        Control.GESTURE -> null
        Control.MIRROR -> null
    }
}

@DrawableRes
internal fun Gesture.toPictureRes(): Int? {
    return when (type) {
        Gesture.Type.PALM -> R.drawable.gesture_stop
        Gesture.Type.THUMB_UP -> R.drawable.gesture_like
        Gesture.Type.THUMB_DOWN -> R.drawable.gesture_dislike
        Gesture.Type.FINGER_TO_LIPS -> R.drawable.gesture_mute
        Gesture.Type.OK -> R.drawable.gesture_ok
        Gesture.Type.NONE -> null
    }
}
