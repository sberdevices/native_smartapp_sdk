@file:Suppress("NonAsciiCharacters")

package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.IInterface
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import ru.sberdevices.common.logger.Logger

/**
 * Тест для [CachedBinderHelper]
 *
 * @author Сидоров Максим
 */
class CachedBinderHelperTest {
    private val intent = mockk<Intent>()
    private val appContext = mockk<Context>()
    private val pm = mockk<PackageManager> {
        every { queryIntentServices(intent, PackageManager.MATCH_ALL) } returns
            mutableListOf(mockk())
    }
    private val context = mockk<Context> {
        every { applicationContext } returns appContext
        every { packageManager } returns pm
    }

    private val logger = Logger.get("CachedBinderHelperTest")

    private val binder = mockk<IBinder>()
    private val binding = mockk<IInterface>()
    private val internalHelper = BinderHelperFactory(
        context = context,
        intent = intent,
        logger = logger,
        getBinding = { binding },
    ).create()

    private val helper = CachedBinderHelper(internalHelper, logger, DISCONNECT_DELAY)

    @Before
    fun prepare() {
        every { appContext.bindService(any(), any(), any()) } answers {
            arg<ServiceConnection>(1).onServiceConnected(mockk(), binder)
            true
        }
        every { appContext.unbindService(any()) } returns Unit
    }

    @Test
    fun `Вложенный connect`() = runBlocking {
        // Do
        helper.connect()

        // check
        assertTrue("helper должен быть connected после первого вызова connect()", helper.hasConnection)
        assertEquals("количество соединений должно быть равно 1", 1, helper.connectionCount)

        // Do
        helper.connect()

        // check
        assertEquals("количество соединений должно быть равно 2", 2, helper.connectionCount)
        verifyBinderConnect(1)
    }

    @Test
    fun `Вложенный disconnect`() = runBlocking {
        // prepare
        helper.connect()
        helper.connect()

        // do
        helper.disconnect()

        // check
        assertTrue("helper должен быть connected, когда количество соединений больше 0", helper.hasConnection)
        assertEquals("количество соединений должно быть равно 1", 1, helper.connectionCount)

        // do
        helper.disconnect()

        // check
        assertTrue("helper должен быть connected, даже когда нет активных соединенмй", helper.hasConnection)
        assertEquals("количество соединений должно быть равно 0", 0, helper.connectionCount)
        assertTrue("должно быть создано задание на отложенный disconnect", helper.hasScheduleDisconnectTask)

        verifyBinderConnect(1)
        verifyBinderDisconnect(0)
    }

    @Test
    fun `Вложенный execute`() = runBlocking {
        // prepare
        helper.connect()

        // do
        val executeResult = helper.execute { 1 }

        // check
        assertEquals(1, executeResult)
        verifyBinderConnect(1)
        verifyBinderDisconnect(0)
        assertEquals(
            "количество соединений не должно изменятся после вызова execute",
            1, helper.connectionCount
        )
    }

    @Test
    fun `execute без connect`() = runBlocking {
        // do
        val result = helper.execute { 1 }

        // check
        assertEquals(1, result)
        verifyBinderConnect(1)
        verifyBinderDisconnect(0)
        assertEquals(
            "количество соединений не должно изменятся после вызова execute",
            0, helper.connectionCount
        )
        assertTrue("Седдинение с сервисом должно остаться активным", helper.hasConnection)
        assertTrue("должно быть создано задание на отложенный disconnect", helper.hasScheduleDisconnectTask)
    }

    @Test
    fun `Отложенный disconnect`() = runBlocking {
        // prepare
        helper.connect()

        // do
        helper.disconnect()

        // check
        assertTrue("helper должен быть connected", helper.hasConnection)
        assertEquals("количество соединений должно быть равно 0", 0, helper.connectionCount)
        assertTrue("должно быть создано задание на отложенный disconnect", helper.hasScheduleDisconnectTask)

        verifyBinderConnect(1)
        verifyBinderDisconnect(0)

        // do
        delay(DISCONNECT_DELAY + 100)

        // check - после таймаута должен быть выполнен реальный disconnect и соединение должно стать не активным
        delay(DISCONNECT_DELAY + 100)
        assertFalse("после выполнения задания, оно должно быть удалено", helper.hasScheduleDisconnectTask)
        assertFalse("Соединение должно стать неактивным", helper.hasConnection)
        verifyBinderDisconnect(1)
    }

    @Test
    fun `connect, когда активно задание на отложенный disconnect`() = runBlocking {
        // prepare
        helper.connect()

        // do
        helper.disconnect()

        // check
        assertTrue("helper должен быть connected", helper.hasConnection)
        assertEquals("количество соединений должно быть равно 0", 0, helper.connectionCount)
        assertTrue("должно быть создано задание на отложенный disconnect", helper.hasScheduleDisconnectTask)
        verifyBinderConnect(1)
        verifyBinderDisconnect(0)

        // do
        helper.connect()
        delay(DISCONNECT_DELAY + 100)

        // check
        assertTrue("helper должен быть connected", helper.hasConnection)
        assertEquals("количество соединений должно быть равно 1", 1, helper.connectionCount)
        assertFalse("должно быть удалено задание на отложенный disconnect", helper.hasScheduleDisconnectTask)
        verifyBinderConnect(1)
        verifyBinderDisconnect(0)
    }

    private fun verifyBinderConnect(exactly: Int) {
        verify(exactly = exactly) { appContext.bindService(intent, any(), Context.BIND_AUTO_CREATE) }
    }

    private fun verifyBinderDisconnect(exactly: Int) {
        verify(exactly = exactly) { appContext.unbindService(any()) }
    }
}

private const val DISCONNECT_DELAY = 500L
