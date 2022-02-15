package ru.sberdevices.camera.factories.snapshot

import android.graphics.Bitmap
import androidx.annotation.MainThread

@MainThread
interface SnapshotCapturedCallback {
    fun onCaptured(pic: Bitmap)
}
