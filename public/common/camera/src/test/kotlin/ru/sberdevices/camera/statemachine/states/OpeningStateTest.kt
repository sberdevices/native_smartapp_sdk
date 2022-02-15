package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class OpeningStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()
    private val state = OpeningState(stateMachineMock)

    @Test
    fun `open camera on enter`() {
        state.onEnter()

        verify(stateMachineMock).openCamera()
    }

    @Test
    fun `goto reclosing on stop`() {
        state.onAction(CameraAction.Stop)

        verify(stateMachineMock).state = any<ReclosingState>()
    }

    @Test
    fun `goto starting session on opened`() {
        state.onAction(CameraAction.Callback.Opened(cameraMock))

        verify(stateMachineMock).state = any<StartingSessionState>()
    }

    @Test
    fun `goto opening on disconnect`() {
        state.onAction(CameraAction.Callback.Disconnected)

        verify(stateMachineMock).state = any<OpeningState>()
    }

    @Test
    fun `goto opening on error`() {
        state.onAction(CameraAction.Callback.Error(error = 4))

        verify(stateMachineMock).state = any<OpeningState>()
    }
}
