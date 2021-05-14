package ru.sberdevices.camera.statemachine

import androidx.annotation.WorkerThread
import ru.sberdevices.common.logger.Logger

@WorkerThread
internal interface ActionDispatcher {
    fun dispatch(action: CameraAction)
}

internal class ActionDispatcherImpl(
    private val stateHolder: StateHolder
) : ActionDispatcher {
    private val logger = Logger.get("ActionDispatcher")

    override fun dispatch(action: CameraAction) {
        logger.debug { "dispatch ${action.javaClass.simpleName}" }
        stateHolder.state.onAction(action)
    }
}
