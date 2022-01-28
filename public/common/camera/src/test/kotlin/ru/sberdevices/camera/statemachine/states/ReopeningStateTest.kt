package ru.sberdevices.camera.statemachine.states

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraAction
import ru.sberdevices.camera.statemachine.CameraStateMachine

internal class ReopeningStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()
    private val state = ReopeningState(stateMachineMock, cameraMock)

    @Test
    fun `close camera on enter and goto opening`() {
        doReturn(false).`when`(cameraMock).close()
        state.onEnter()
        verify(cameraMock).close()
        verify(stateMachineMock).state = any<OpeningState>()
    }

    @Test
    fun `close camera on enter stay in reopening`() {
        doReturn(true).`when`(cameraMock).close()
        state.onEnter()
        verify(cameraMock).close()
        verify(stateMachineMock, never()).state = any<OpeningState>()
    }

    @Test
    fun `goto opening on disconnected`() {
        state.onAction(CameraAction.Start)
        state.onAction(CameraAction.Callback.Disconnected)
        verify(stateMachineMock).state = any<OpeningState>()
    }

    @Test
    fun `goto closing on disconnected`() {
        state.onAction(CameraAction.Stop)
        state.onAction(CameraAction.Callback.Disconnected)
        verify(stateMachineMock).state = any<ClosedState>()
    }
}
