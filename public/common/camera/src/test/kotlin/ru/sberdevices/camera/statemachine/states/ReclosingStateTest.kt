package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class ReclosingStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()
    private val state = ReclosingState(stateMachineMock)

    @Test
    fun `goto starting session on opened if was started`() {
        state.onAction(CameraAction.Start)
        state.onAction(CameraAction.Callback.Opened(cameraMock))

        verify(stateMachineMock).state = any<StartingSessionState>()
    }

    @Test
    fun `goto closing on opened if wasn't started during opening`() {
        state.onAction(CameraAction.Callback.Opened(cameraMock))
        verify(stateMachineMock).state = any<ClosingState>()
    }

    @Test
    fun `goto closed on error`() {
        state.onAction(CameraAction.Callback.Error(error = 4))
        verify(stateMachineMock).state = any<ClosedState>()
    }

    @Test
    fun `goto closed on disconnect`() {
        state.onAction(CameraAction.Callback.Disconnected)
        verify(stateMachineMock).state = any<ClosedState>()
    }
}
