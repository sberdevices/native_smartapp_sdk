package ru.sberdevices.services.mic.camera.state.aidl;

import ru.sberdevices.services.mic.camera.state.aidl.IOnMicCameraStateChangedListener;

interface IMicCameraStateService {
    const int VERSION = 1;

    oneway void setCameraEnabled(boolean isEnabled) = 1;

    oneway void registerMicCameraStateListener(IOnMicCameraStateChangedListener listener) = 10;

    oneway void unregisterMicCameraStateListener(IOnMicCameraStateChangedListener listener) = 11;
}
