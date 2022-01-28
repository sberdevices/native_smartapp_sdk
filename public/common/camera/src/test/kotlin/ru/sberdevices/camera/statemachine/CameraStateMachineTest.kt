package ru.sberdevices.camera.statemachine

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.sberdevices.camera.factories.camera.CameraOpener
import ru.sberdevices.camera.factories.snapshot.SnapshotCapturedCallback
import ru.sberdevices.camera.statemachine.states.ClosedState
import ru.sberdevices.camera.utils.CameraExceptionHandler

private const val CAMERA_ID = "0"

class CameraStateMachineTest {
    private val dispatcherMock = mock<ActionDispatcher>()
    private val openerMock = mock<CameraOpener>()
    private val exceptionHandler = mock<CameraExceptionHandler>()
    private val stateHolder = StateHolderImpl()

    private val stateMachine = CameraStateMachineImpl(
        stateHolder = stateHolder,
        dispatcher = dispatcherMock,
        cameraOpener = openerMock,
        sessionOpener = mock(),
        previewCallbackFactory = mock(),
        snapshotCallbackFactory = mock(),
        imageReaderFactory = mock(),
        cameraHandler = mock(),
        exceptionHandler = exceptionHandler,
    )

    @Test
    fun `state machine starts in ClosedState`() {
        stateHolder.init(stateMachine)

        assertTrue(stateMachine.state is ClosedState)
    }

    @Test
    fun `dispatch start on start`() {
        stateMachine.start(CAMERA_ID, emptyList())

        verify(dispatcherMock).dispatch(eq(CameraAction.Start))
    }

    @Test
    fun `dispatch stop on stop`() {
        stateMachine.stop()

        verify(dispatcherMock).dispatch(eq(CameraAction.Stop))
    }

    @Test
    fun `dispatch snapshot on snapshot`() {
        val requestCaptor = argumentCaptor<CameraAction.Snapshot>()
        val callback = mock<SnapshotCapturedCallback>()

        stateMachine.snapshot(callback)

        verify(dispatcherMock).dispatch(requestCaptor.capture())
        assertEquals(callback, requestCaptor.firstValue.callback)
    }

    @Test
    fun `open camera, correct camera on open`() {
        val stringCaptor = argumentCaptor<String>()

        stateMachine.cameraId = "123"
        stateMachine.openCamera()

        verify(openerMock).openCamera(stringCaptor.capture())
        assertEquals("123", stringCaptor.firstValue)
    }
}
