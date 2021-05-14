package ru.sberdevices.cv.detection

import kotlinx.coroutines.flow.Flow
import ru.sberdevices.cv.ServiceInfo
import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.Humans
import ru.sberdevices.cv.detection.entity.humans.HumansDetectionAspect

/**
 * This interface allows to observe computer vision detections. There is information about face and
 * body points (face landmarks and pose), body segmentation (mask), face and body bounding boxes,
 * gestures, mirror presence. Moreover, client is able to get api version used by client and complete
 * data on versions, hashs and metadata used by service.
 *
 * The interface allows client cooperate with computer vision Android service process by aidl IPC
 * with [android.os.Binder]. Api handles DeadObjectException. In case of service crashes api
 * binding restores service connections and observed detections flows. Service connection is
 * defended with dangerous level permission ru.sberdevices.permission.COMPUTER_VISION_SENSITIVE.
 * When api is no longer needed, it should be closed with close() method call.
 */
@SuppressWarnings("unused") // API class
interface CvApi : AutoCloseable {
    override fun close()

    fun observeHumans(aspects: Set<HumansDetectionAspect>): Flow<Humans>
    fun observeGestures(): Flow<Gesture>
    fun observeIsMirrorDetected(): Flow<Boolean>

    suspend fun getVersion(): String?
    suspend fun getServiceInfo(): ServiceInfo?
}
