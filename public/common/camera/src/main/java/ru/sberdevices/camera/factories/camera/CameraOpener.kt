package ru.sberdevices.camera.factories.camera

import android.Manifest
import android.hardware.camera2.CameraManager
import android.os.Handler
import androidx.annotation.RequiresPermission

interface CameraOpener {
    @RequiresPermission(Manifest.permission.CAMERA)
    fun openCamera(cameraId: String)
}

internal class CameraOpenerImpl(
    private val cameraManager: CameraManager,
    private val connectionCallback: ConnectionCallback,
    private val cameraHandler: Handler,
) : CameraOpener {

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun openCamera(cameraId: String) {
        cameraManager.openCamera(cameraId, connectionCallback, cameraHandler)
    }
}
