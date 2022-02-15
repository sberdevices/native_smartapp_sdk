package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.session.CameraSession
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class SessionStartedStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()
    private val cameraSessionMock: CameraSession = mock()
    private val state = SessionStartedState(stateMachineMock, cameraMock, cameraSessionMock)

    @Test
    fun `goto session started on session configured`() {
        state.onAction(CameraAction.Callback.SessionConfigured(cameraSessionMock))
        verify(cameraSessionMock).close()
        verify(stateMachineMock).state = any<StartingSessionState>()
    }

    @Test
    fun `goto closing on session failed`() {
        state.onAction(CameraAction.Callback.SessionFailed)
        verify(stateMachineMock).state = any<ClosingState>()
    }
}
