package ru.sberdevices.camera.factories.preview

import ru.sberdevices.camera.statemachine.ActionDispatcher

internal interface PreviewCallbackFactory {
    fun create(): PreviewCallback
}

internal class PreviewCallbackFactoryImpl(private val dispatcher: ActionDispatcher) : PreviewCallbackFactory {
    override fun create(): PreviewCallback {
        return PreviewCallback(dispatcher)
    }
}
