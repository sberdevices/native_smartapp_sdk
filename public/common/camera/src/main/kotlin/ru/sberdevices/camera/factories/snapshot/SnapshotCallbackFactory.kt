package ru.sberdevices.camera.factories.snapshot

internal interface SnapshotCallbackFactory {
    fun create(): SnapshotCallback
}

internal class SnapshotCallbackFactoryImpl : SnapshotCallbackFactory {
    override fun create(): SnapshotCallback {
        return SnapshotCallback()
    }
}
