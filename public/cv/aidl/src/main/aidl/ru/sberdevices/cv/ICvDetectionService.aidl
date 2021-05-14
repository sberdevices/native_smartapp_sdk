package ru.sberdevices.cv;

import ru.sberdevices.cv.ServiceInfo;
import ru.sberdevices.cv.detection.entity.IHumansDetectionListener;
import ru.sberdevices.cv.detection.entity.IGestureDetectionListener;
import ru.sberdevices.cv.detection.entity.IMirrorDetectedListener;
import ru.sberdevices.cv.IDeathListener;

interface ICvDetectionService {
    const int VERSION = 4;
    // reserved 110, 120, 210, 220, 330

    String getVersion() = 10;
    ServiceInfo getServiceInfo() = 20;
    int getBindingId() = 30;

    oneway void subscribeForHumansDetection(in int bindingId, IHumansDetectionListener listener, in byte[] aspectCodes) = 130;
    oneway void subscribeForGestureDetection(in int bindingId, IGestureDetectionListener listener) = 140;
    oneway void unsubscribeFromHumansDetection(in int bindingId, IHumansDetectionListener listener) = 150;
    oneway void unsubscribeFromGestureDetection(in int bindingId, IGestureDetectionListener listener) = 160;
    oneway void subscribeForIsMirrorDetected(in int bindingId, IMirrorDetectedListener listener) = 170;
    oneway void unsubscribeFromIsMirrorDetected(in int bindingId, IMirrorDetectedListener listener) = 180;
    oneway void sendClientCvApiVersion(in int bindingId, String version) = 310;
    oneway void sendDeathListener(in int bindingId, IDeathListener deathListener) = 320;
}
