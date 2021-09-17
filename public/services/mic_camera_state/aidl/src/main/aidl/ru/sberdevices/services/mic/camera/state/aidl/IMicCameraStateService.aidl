package ru.sberdevices.services.mic.camera.state.aidl;

import ru.sberdevices.services.mic.camera.state.aidl.IOnMicCameraStateChangedListener;

interface IMicCameraStateService {
    const int VERSION = 1;

    void setCameraEnabled(boolean isEnabled) = 1;

    void setMicEnabled(boolean isEnabled) = 2;

    void registerMicCameraStateListener(IOnMicCameraStateChangedListener listener) = 10;

    void unregisterMicCameraStateListener(IOnMicCameraStateChangedListener listener) = 11;
}
