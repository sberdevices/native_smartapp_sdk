package ru.sberdevices.cv.detection.util

import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.Humans
import ru.sberdevices.cv.detection.entity.humans.HumansDetectionAspect
import ru.sberdevices.cv.detection.entity.humans.Point
import ru.sberdevices.cv.proto.BodyMask
import ru.sberdevices.cv.proto.BodyMasks
import ru.sberdevices.cv.proto.BoundingBox
import ru.sberdevices.cv.proto.BoundingBoxes
import ru.sberdevices.cv.proto.DetectionEntity
import ru.sberdevices.cv.proto.Landmarks
import ru.sberdevices.cv.proto.PointGroup

/**
 * Converters between bytes and types
 */

fun ByteArray.toHumans(aspects: Set<HumansDetectionAspect>, lastHumans: Humans): Humans? {
    return DetectionEntity.newBuilder()
        .mergeFrom(this)
        .build()
        ?.toHumans(aspects, lastHumans)
}

private fun DetectionEntity.toHumans(aspects: Set<HumansDetectionAspect>, lastHumans: Humans): Humans? {
    return humans?.run {
        Humans(
            bodyBoundingBoxes = (if (withBodyBoundingBoxes(aspects)) bodyBoundingBoxes.convertFromProto() else null)
                ?: lastHumans.bodyBoundingBoxes.takeIf { HumansDetectionAspect.Body.BoundingBox in aspects },
            faceBoundingBoxes = (if (withFaceBoundingBoxes(aspects)) faceBoundingBoxes.convertFromProto() else null)
                ?: lastHumans.faceBoundingBoxes.takeIf { HumansDetectionAspect.Face.BoundingBox in aspects },
            faceTrackIds = bodyTrackIdsList,
            bodyLandmarks = (if (withBodyLandmarks(aspects)) bodyLandmarks.convertFromProto() else null)
                ?: lastHumans.bodyLandmarks.takeIf {
                    HumansDetectionAspect.Body.Landmarks.SentalNet in aspects ||
                        HumansDetectionAspect.Body.Landmarks.HomaNet in aspects
                },
            faceLandmarks = (if (withFaceLandmarks(aspects)) faceLandmarks.convertFromProto() else null)
                ?: lastHumans.faceLandmarks.takeIf { HumansDetectionAspect.Face.Landmarks in aspects },
            bodyTrackIds = faceTrackIdsList,
            bodyMask = (if (withBodyMask(aspects)) bodyMasks.convertFromProto() else null)
                ?: lastHumans.bodyMask.takeIf { HumansDetectionAspect.Body.Segmentation in aspects }
        )
    }
}

private fun ru.sberdevices.cv.proto.Humans.withBodyBoundingBoxes(aspects: Set<HumansDetectionAspect>): Boolean {
    return HumansDetectionAspect.Body.BoundingBox in aspects && hasBodyBoundingBoxes()
}

private fun ru.sberdevices.cv.proto.Humans.withFaceBoundingBoxes(aspects: Set<HumansDetectionAspect>): Boolean {
    return HumansDetectionAspect.Face.BoundingBox in aspects && hasFaceBoundingBoxes()
}

private fun ru.sberdevices.cv.proto.Humans.withFaceLandmarks(aspects: Set<HumansDetectionAspect>): Boolean {
    return HumansDetectionAspect.Face.Landmarks in aspects && hasFaceLandmarks()
}

private fun ru.sberdevices.cv.proto.Humans.withBodyLandmarks(aspects: Set<HumansDetectionAspect>): Boolean {
    return (
        HumansDetectionAspect.Body.Landmarks.HomaNet in aspects ||
            HumansDetectionAspect.Body.Landmarks.SentalNet in aspects
        ) &&
        hasBodyLandmarks()
}

private fun ru.sberdevices.cv.proto.Humans.withBodyMask(aspects: Set<HumansDetectionAspect>): Boolean {
    return HumansDetectionAspect.Body.Segmentation in aspects && hasBodyMasks() && bodyMasks.masksCount > 0
}

private fun BoundingBoxes.convertFromProto(): ru.sberdevices.cv.detection.entity.humans.BoundingBoxes {
    return ru.sberdevices.cv.detection.entity.humans.BoundingBoxes(
        boundingBoxes = boundingBoxList.map { it.convertFromProto() },
        frameTimestampMs = frameTimestampMs
    )
}

private fun BoundingBox.convertFromProto(): ru.sberdevices.cv.entity.BoundingBox {
    return ru.sberdevices.cv.entity.BoundingBox(
        relativeLeft = relativeLeft,
        relativeTop = relativeTop,
        relativeRight = relativeRight,
        relativeBottom = relativeBottom
    )
}

private fun Landmarks.convertFromProto(): ru.sberdevices.cv.detection.entity.humans.Landmarks {
    return ru.sberdevices.cv.detection.entity.humans.Landmarks(
        pointGroups = pointGroupsList.map { it.convertFromProto() },
        frameTimestampMs = frameTimestampMs
    )
}

private fun PointGroup.convertFromProto(): ru.sberdevices.cv.detection.entity.humans.PointGroup {
    return ru.sberdevices.cv.detection.entity.humans.PointGroup(
        points = pointList.map { it.convertFromProto() },
        trackId = if (hasTrackId()) trackId.value else null
    )
}

private fun ru.sberdevices.cv.proto.Point.convertFromProto(): Point {
    return Point(
        relativeX = relativeX,
        relativeY = relativeY,
        confidence = if (hasConfidence()) confidence.value else null
    )
}

private fun BodyMasks.convertFromProto(): ru.sberdevices.cv.detection.entity.humans.BodyMask {
    return masksList.first().convertFromProto(frameTimestampMs)
}

private fun BodyMask.convertFromProto(detectionTimeMs: Long): ru.sberdevices.cv.detection.entity.humans.BodyMask {
    return ru.sberdevices.cv.detection.entity.humans.BodyMask(
        data = data.toByteArray(),
        rowCount = rowCount,
        columnCount = columnCount,
        frameTimestampMs = detectionTimeMs
    )
}

fun ByteArray.toGesture(): Gesture? {
    return DetectionEntity.newBuilder()
        .mergeFrom(this)
        .build()
        .convertFromProto()
}

private fun DetectionEntity.convertFromProto(): Gesture? {
    return gesture?.run {
        Gesture(
            frameTimestampMs = frameTimestampMs,
            type = Gesture.Type.fromCode(gesture.typeValue.toByte()),
            metadata = if (gesture.metadata.isNullOrBlank()) null else gesture.metadata
        )
    }
}
