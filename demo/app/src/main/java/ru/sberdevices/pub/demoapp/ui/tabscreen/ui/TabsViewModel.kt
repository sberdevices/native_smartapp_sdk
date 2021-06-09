package ru.sberdevices.pub.demoapp.ui.tabscreen.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sberdevices.cv.detection.CvApiFactory

/**
 * View model for main tabs fragment
 */
class TabsViewModel(cvApiFactory: CvApiFactory) : ViewModel() {
    
    private val _isCvAvailableOnDevice: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isCvAvailableOnDevice = _isCvAvailableOnDevice.asStateFlow()
    
    init {
        val cvApi = cvApiFactory.get()
        _isCvAvailableOnDevice.tryEmit(cvApi.isAvailableOnDevice())
        cvApi.close()
    }
}