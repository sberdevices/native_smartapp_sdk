package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class CoveredStateTest {
    private val stateMachineMock = mock<CameraStateMachine>()
    private val cameraMock = mock<Camera>()

    @Test
    fun `close camera on enter`() {
        val state = CoveredState(stateMachineMock, camera = cameraMock, openAfterUncover = true)

        state.onEnter()

        verify(cameraMock).close()
    }

    @Test
    fun `close opened camera on opened`() {
        val state = CoveredState(stateMachineMock, camera = null, openAfterUncover = true)

        state.onAction(CameraAction.Callback.Opened(cameraMock))

        verify(cameraMock).close()
    }

    @Test
    fun `goto opening on uncover event`() {
        val state = CoveredState(stateMachineMock, camera = null, openAfterUncover = true)

        state.onAction(CameraAction.Uncovered)

        verify(stateMachineMock).state = any<OpeningState>()
    }

    @Test
    fun `goto closed on uncover event if openAfterUncover is false`() {
        val state = CoveredState(stateMachineMock, camera = null, openAfterUncover = false)

        state.onAction(CameraAction.Uncovered)

        verify(stateMachineMock).state = any<ClosedState>()
    }

    @Test
    fun `goto closed on uncover event if called stop before`() {
        val state = CoveredState(stateMachineMock, camera = null, openAfterUncover = true)

        state.onAction(CameraAction.Stop)
        state.onAction(CameraAction.Uncovered)

        verify(stateMachineMock).state = any<ClosedState>()
    }
}
