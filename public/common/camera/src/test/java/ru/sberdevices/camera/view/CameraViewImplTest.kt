@file:Suppress("NonAsciiCharacters")

package ru.sberdevices.camera.view

import android.view.SurfaceHolder
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import ru.sberdevices.camera.controller.CameraController

class CameraViewImplTest {

    @Test
    fun `создание cameraView, добавляется callback`() {
        RunTest {
            // prepare
            // do
            createCameraView()

            // verify
            verify { surfaceHolder.addCallback(any()) }
        }
    }

    @Test
    fun `создание cameraView с несозданной surface, не стартует controller`() {
        RunTest(surfaceCreated = false) {
            // prepare
            // do
            createCameraView()

            // verify
            verify(exactly = 0) { cameraController.start(any(), any()) }
        }
    }

    @Test
    fun `создание cameraView с созданной невалидной surface, не стартует controller`() {
        RunTest(surfaceCreated = true, surfaceValid = false) {
            // prepare
            // do
            createCameraView()

            // verify
            verify(exactly = 0) { cameraController.start(any(), any()) }
        }
    }

    @Test
    fun `создание cameraView с уже созданной валидной surface, стартует controller`() {
        RunTest {
            // prepare
            // do
            createCameraView()

            // verify
            verify { cameraController.start(any(), any()) }
        }
    }

    @Test
    fun `остановка controller в незапущенном состоянии, controller не останавливается`() {
        RunTest(surfaceCreated = false, surfaceValid = true) {
            // prepare
            createCameraView()

            // do
            callback.surfaceDestroyed(surfaceHolder)

            // verify
            verify(exactly = 0) { cameraController.stop() }
        }
    }

    @Test
    fun `остановка controller в запущенном состоянии, controller останавливается`() {
        RunTest(surfaceCreated = true, surfaceValid = true) {
            // prepare
            createCameraView()

            // do
            callback.surfaceCreated(surfaceHolder)
            callback.surfaceDestroyed(surfaceHolder)

            // verify
            verify(exactly = 1) { cameraController.stop() }
        }
    }

    @Test
    fun `изменение surface, перезапуск controller`() {
        RunTest(surfaceCreated = true, surfaceValid = true) {
            // prepare
            createCameraView()

            // do
            callback.surfaceChanged(surfaceHolder, 0, 0, 0)

            // verify
            verifyOrder {
                cameraController.stop()
                cameraController.start(any(), any())
            }
        }
    }

    @Test
    fun `создание surface первый раз, запуск controller`() {
        RunTest(surfaceCreated = false, surfaceValid = true) {
            // prepare
            createCameraView()

            // do
            callback.surfaceCreated(surfaceHolder)

            // verify
            verify { cameraController.start(any(), any()) }
        }
    }

    @Test
    fun `destroy surface, остановка controller`() {
        RunTest(surfaceCreated = true, surfaceValid = true) {
            // prepare
            createCameraView()

            // do
            callback.surfaceDestroyed(surfaceHolder)

            // verify
            verify { cameraController.stop() }
        }
    }

    class RunTest(
        surfaceCreated: Boolean = true,
        surfaceValid: Boolean = true,
        testCase: RunTest.() -> Unit,
    ) {
        private val callbackSlot = slot<SurfaceHolder.Callback>()
        val surfaceHolder = mockk<SurfaceHolder>(relaxed = true)
        val cameraController = mockk<CameraController>(relaxed = true)
        val callback: SurfaceHolder.Callback
            get() = callbackSlot.captured

        fun createCameraView(): CameraView {
            return CameraViewImpl(surfaceHolder, cameraController)
        }

        init {
            every { surfaceHolder.isCreating } returns !surfaceCreated
            every { surfaceHolder.surface.isValid } returns surfaceValid
            every { surfaceHolder.addCallback(capture(callbackSlot)) } just Runs
            runBlockingTest { testCase() }
        }
    }
}
