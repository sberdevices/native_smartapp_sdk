package ru.sberdevices.pub.demoapp.ui.cv

import ru.sberdevices.pub.demoapp.ui.cv.entity.Control

internal data class ComputerVisionViewState(
    val enabledAspects: Set<Control>,
    val isMirrorDetected: Boolean?,
    val permissionsGranted: Boolean
)
