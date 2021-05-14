package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class ClosedStateTest {
    private val stateMachineMock = mock<CameraStateMachine>()
    private val state = ClosedState(stateMachineMock)

    @Test
    fun `goto opening on start`() {
        state.onAction(CameraAction.Start)

        verify(stateMachineMock).state = any<OpeningState>()
    }

    @Test
    fun `goto closing on opened`() {
        state.onAction(CameraAction.Callback.Opened(mock()))

        verify(stateMachineMock).state = any<ClosingState>()
    }
}
