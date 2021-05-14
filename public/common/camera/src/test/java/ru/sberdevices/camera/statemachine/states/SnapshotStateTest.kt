package ru.sberdevices.camera.statemachine.states

import android.media.ImageReader
import android.util.Size
import android.view.Surface
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import ru.sberdevices.camera.factories.camera.Camera
import ru.sberdevices.camera.statemachine.CameraStateMachine

class SnapshotStateTest {
    private val stateMachineMock: CameraStateMachine = mock()
    private val cameraMock: Camera = mock()
    private val imageReaderMock: ImageReader = mock()

    @Before
    fun `before each test`() {
        doReturn(Size(0, 0)).`when`(cameraMock).getMaxSize()
        doReturn(emptyList<Surface>()).`when`(stateMachineMock).surfaces
        doReturn(imageReaderMock).`when`(stateMachineMock).createSnapshotImageReader(any())
        doReturn(mock<Surface>()).`when`(imageReaderMock).surface
    }

    @Test
    fun `create new session on enter`() {
        doReturn(true).`when`(stateMachineMock).openSession(any(), any())
        val state = SnapshotState(stateMachineMock, cameraMock, mock())

        state.onEnter()

        verify(stateMachineMock).openSession(any(), any())
        verify(stateMachineMock, never()).state = any<ReopeningState>()
    }

    @Test
    fun `goto closing on enter if session wasn't started`() {
        doReturn(false).`when`(stateMachineMock).openSession(any(), any())
        val state = SnapshotState(stateMachineMock, cameraMock, mock())

        state.onEnter()

        verify(stateMachineMock).openSession(any(), any())
        verify(stateMachineMock).state = any<ReopeningState>()
    }
}
