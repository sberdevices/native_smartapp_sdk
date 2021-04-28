package ru.sberdevices.services.mic.camera.state.aidl;

interface IOnMicCameraStateChangedListener {
    const int VERSION = 1;

    oneway void onCameraMicStateChanged(boolean isMicDisabled, boolean isCameraDisabled, boolean isCameraCovered) = 10;
}
