@file:Suppress("NonAsciiCharacters")

package ru.sberdevices.common.binderhelper

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.IInterface
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import ru.sberdevices.common.binderhelper.entities.BinderState

/**
 * Тест для [BinderHelper]
 *
 * @author Илья Богданович on 12.02.2021
 */
class BinderHelperTest {
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
    private val binding = mockk<IInterface>()

    private val helper = BinderHelperFactory(
        context = context,
        intent = intent,
        getBinding = { binding },
    ).create()
    private val scope = TestCoroutineScope()

    @Test
    fun `Успешный connect`() = runBlocking {
        // Prepare
        every { appContext.bindService(any(), any(), any()) } returns true

        // Do
        val result = helper.connect()

        // Check
        verify(exactly = 1) { appContext.bindService(intent, any(), Context.BIND_AUTO_CREATE) }
        assertTrue(result)
    }

    @Test
    fun `Неуспешный connect если сервис отсутствует`() = runBlocking {
        // Prepare
        every { pm.queryIntentServices(intent, PackageManager.MATCH_ALL) } returns mutableListOf()

        // Do
        val result = helper.connect()

        // Check
        verify(inverse = true) { appContext.bindService(any(), any(), any()) }
        assertFalse(result)
    }

    @Test
    fun `disconnect без connect`() {
        // Prepare

        // Do
        helper.disconnect()

        // Check
        verify(inverse = true) { appContext.unbindService(any()) }
    }

    @Test
    fun `connect and disconnect`() = runBlocking {
        // Prepare
        every { appContext.bindService(any(), any(), any()) } returns true
        every { appContext.unbindService(any()) } returns Unit

        // Do
        helper.connect()
        helper.disconnect()

        // Check
        verifySequence {
            appContext.bindService(intent, any(), Context.BIND_AUTO_CREATE)
            appContext.unbindService(any())
        }
    }

    @Test
    fun `Вызов execute без connect висит в ожидании`() = runBlocking {
        // Prepare
        val method = mockk<(Any) -> Unit>(relaxUnitFun = true)

        // Do
        scope.launch { helper.execute(method) }

        // Check
        verify(inverse = true) { method.invoke(any()) }
    }

    @Test
    fun `Вызов execute после connect возвращает результат`() = runBlocking {
        // Prepare
        val binder = mockk<IBinder>()
        every { appContext.bindService(any(), any(), any()) } answers {
            arg<ServiceConnection>(1).onServiceConnected(mockk(), binder)
            true
        }

        // Do
        helper.connect()
        val result = helper.execute { 1 }

        // Check
        assertEquals(1, result)
    }

    @Test
    fun `Вызов tryExecute без connect возвращает null`() = runBlocking {
        // Prepare

        // Do
        val result = helper.tryExecute { 1 }

        // Check
        assertNull(result)
    }

    @Test
    fun `Вызов tryExecute после connect возвращает результат`() = runBlocking {
        // Prepare
        val binder = mockk<IBinder>()
        every { appContext.bindService(any(), any(), any()) } answers {
            arg<ServiceConnection>(1).onServiceConnected(mockk(), binder)
            true
        }

        // Do
        helper.connect()
        val result = helper.tryExecute { 1 }

        // Check
        assertEquals(1, result)
    }

    @Test
    fun `Умерший биндинг приводит к реконнекту`() = runBlocking {
        // Prepare
        every { appContext.bindService(any(), any(), any()) } answers {
            arg<ServiceConnection>(1).onBindingDied(mockk())
            true
        } andThen(true)

        // Do
        helper.connect()

        // Check
        verify(exactly = 2) {
            appContext.bindService(intent, any(), Context.BIND_AUTO_CREATE)
        }
        assertThat(helper.binderStateFlow.value, equalTo(BinderState.BINDING_DIED))
    }

    @Test
    fun `Вызываем обработчик null binding`() = runBlocking {
        // Prepare
        every { appContext.bindService(any(), any(), any()) } answers {
            arg<ServiceConnection>(1).onNullBinding(mockk())
            true
        } andThen(true)

        // Do
        helper.connect()

        // Check
        coVerify(exactly = 1) {
            appContext.bindService(intent, any(), Context.BIND_AUTO_CREATE)
        }
        assertThat(helper.binderStateFlow.value, equalTo(BinderState.NULL_BINDING))
    }

    @Test
    fun `Вызываем обработчик disconnect`() = runBlocking {
        // Prepare
        every { appContext.bindService(any(), any(), any()) } answers {
            arg<ServiceConnection>(1).onServiceDisconnected(mockk())
            true
        } andThen(true)

        // Do
        helper.connect()

        // Check
        coVerify(exactly = 1) {
            appContext.bindService(intent, any(), Context.BIND_AUTO_CREATE)
        }
        assertThat(helper.binderStateFlow.value, equalTo(BinderState.DISCONNECTED))
    }

}
