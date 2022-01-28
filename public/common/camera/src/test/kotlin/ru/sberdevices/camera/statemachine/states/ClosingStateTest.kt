package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class ClosingStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()

    @Test
    fun `goto reopening on start`() {
        val state = ClosingState(stateMachineMock, cameraMock, restart = true)

        state.onAction(CameraAction.Start)

        verify(stateMachineMock).state = any<ReopeningState>()
    }

    @Test
    fun `stop restarting on stop`() {
        val state = ClosingState(stateMachineMock, cameraMock, restart = true)

        state.onAction(CameraAction.Stop)

        Assert.assertFalse(state.restart)
    }

    @Test
    fun `goto closing on opened`() {
        val state = ClosingState(stateMachineMock, cameraMock, restart = true)

        state.onAction(CameraAction.Callback.Opened(cameraMock))

        verify(stateMachineMock).state = any<ClosingState>()
    }

    @Test
    fun `goto opening on disconnected`() {
        val state = ClosingState(stateMachineMock, cameraMock, restart = true)

        state.onAction(CameraAction.Callback.Disconnected)

        verify(stateMachineMock).state = any<OpeningState>()
    }

    @Test
    fun `goto closed on disconnected`() {
        val state = ClosingState(stateMachineMock, cameraMock, restart = false)

        state.onAction(CameraAction.Callback.Disconnected)

        verify(stateMachineMock).state = any<ClosedState>()
    }

    @Test
    fun `goto reopening on error`() {
        val state = ClosingState(stateMachineMock, cameraMock, restart = true)

        state.onAction(CameraAction.Callback.Error(error = 4))

        verify(stateMachineMock).state = any<ReopeningState>()
    }

    @Test
    fun `goto closed on error`() {
        val state = ClosingState(stateMachineMock, cameraMock, restart = false)

        state.onAction(CameraAction.Callback.Error(error = 4))

        verify(stateMachineMock).state = any<ClosedState>()
    }
}
