package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class OpenedStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()
    private val state = object : OpenedState(stateMachineMock, cameraMock) {}

    @Test
    fun `goto closing on stop`() {
        state.onAction(CameraAction.Stop)

        verify(stateMachineMock).state = any<ClosingState>()
    }

    @Test
    fun `goto closing on opened`() {
        state.onAction(CameraAction.Callback.Opened(cameraMock))

        verify(stateMachineMock).state = any<ClosingState>()
    }

    @Test
    fun `goto closed on disconnect`() {
        state.onAction(CameraAction.Callback.Disconnected)

        verify(stateMachineMock).state = any<ClosedState>()
    }

    @Test
    fun `goto reopening on error`() {
        state.onAction(CameraAction.Callback.Error(error = 4))

        verify(stateMachineMock).state = any<ReopeningState>()
    }
}
