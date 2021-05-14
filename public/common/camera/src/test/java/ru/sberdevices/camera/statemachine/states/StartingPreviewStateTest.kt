package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.factories.session.CameraSession
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class StartingPreviewStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()
    private val cameraSessionMock: CameraSession = mock()
    private val state = StartingPreviewState(stateMachineMock, cameraMock, cameraSessionMock)

    @Test
    fun `goto preview started on preview started`() {
        state.onAction(CameraAction.Callback.PreviewStarted)

        verify(stateMachineMock).state = any<PreviewStartedState>()
    }
}
