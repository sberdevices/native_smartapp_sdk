package ru.sberdevices.pub.demoapp.ui.assistant

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AssistantViewModel : ViewModel() {

    private val _shadeState = MutableLiveData<Boolean>()
    val shadeState: MutableLiveData<Boolean> = _shadeState

    fun setShadeShown(shown: Boolean) {
        _shadeState.value = shown
    }
}
