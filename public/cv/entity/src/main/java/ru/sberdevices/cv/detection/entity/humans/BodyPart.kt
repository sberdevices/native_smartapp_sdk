package ru.sberdevices.cv.detection.entity.humans

enum class BodyPart(val pointId: Int, val description: String) {
    NOSE(0, "Nose"),
    LEFT_EYE(1, "Left Eye"),
    RIGHT_EYE(2, "Right Eye"),
    LEFT_EAR(3, "Left Ear"),
    RIGHT_EAR(4, "Right Ear"),
    LEFT_SHOULDER(5, "Left Shoulder"),
    RIGHT_SHOULDER(6, "Right Shoulder"),
    LEFT_ELBOW(7, "Left Elbow"),
    RIGHT_ELBOW(8, "Right Elbow"),
    LEFT_WRIST(9, "Left Wrist"),
    RIGHT_WRIST(10, "Right Wrist"),
    LEFT_HIP(11, "Left Hip"),
    RIGHT_HIP(12, "Right Hip"),
    LEFT_KNEE(13, "Left Knee"),
    RIGHT_KNEE(14, "Right Knee"),
    LEFT_ANKLE(15, "Left Ankle"),
    RIGHT_ANKLE(16, "Right Ankle");
}
