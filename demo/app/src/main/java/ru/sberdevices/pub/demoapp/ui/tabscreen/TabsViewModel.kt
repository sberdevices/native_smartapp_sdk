package ru.sberdevices.pub.demoapp.ui.tabscreen

import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * View model for main tabs fragment
 */
class TabsViewModel : ViewModel() {
    
    private val _isCameraAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isCameraAvailable = _isCameraAvailable.asStateFlow()
    
    init {
        _isCameraAvailable.tryEmit(
            Build.MODEL in DEVICES_WITH_CAMERA
        )
    }

    companion object {
        val DEVICES_WITH_CAMERA = listOf("SberPortal")
    }
}