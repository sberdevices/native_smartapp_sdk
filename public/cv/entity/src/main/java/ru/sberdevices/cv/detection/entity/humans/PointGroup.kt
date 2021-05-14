package ru.sberdevices.cv.detection.entity.humans

data class PointGroup(
    val points: List<Point>,
    val trackId: Int?
)

fun PointGroup.getPoint(bodyPart: BodyPart): Point? {
    return points.getOrNull(bodyPart.pointId)
}
