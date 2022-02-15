package ru.sberdevices.camera.statemachine

import ru.sberdevices.camera.statemachine.states.ClosedState
import ru.sberdevices.common.logger.Logger

internal interface StateHolder {
    var state: CameraState
}

internal class StateHolderImpl : StateHolder {
    private val logger = Logger.get("StateHolder")

    private var _state: CameraState? = null

    override var state: CameraState
        get(): CameraState = requireNotNull(_state)
        set(newState) {
            _state?.let { oldState ->
                oldState.onLeave()
                logger.debug { "Changing state [${oldState.javaClass.simpleName}] -> [${newState.javaClass.simpleName}]" }
            }
            _state = newState
            newState.onEnter()
        }

    fun init(stateMachine: CameraStateMachine) {
        state = ClosedState(stateMachine)
    }
}
