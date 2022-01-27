@file:Suppress("NonAsciiCharacters")

package ru.sberdevices.common.logger.mode

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

/**
 * Тесты для [LogLevelRepositoryImpl]
 * @author Николай Пахомов on 04.08.2021
 */
class LogLevelRepositoryImplTest {

    private val spEditor: SharedPreferences.Editor = mockk(relaxed = true) {
        every { putString(any(), any()) } returns this
    }
    private val sp: SharedPreferences = mockk(relaxed = true) {
        every { edit() } returns spEditor
    }
    private val context: Context = mockk(relaxed = true) {
        every { getSharedPreferences(any(), any()) } returns sp
    }

    private val underTest = LogLevelRepositoryImpl(
        context = context,
        isDebugBuild = false
    )

    @Test
    fun `Если дебажный билд, то работы с SharedPrefs не будет`() {
        // Prepare
        val underTest = LogLevelRepositoryImpl(
            context = context,
            isDebugBuild = true
        )

        // Do
        val mode = underTest.getCurrentLogLevel()
        underTest.setCurrentLogLevel(LogLevel.INFO)
        val mode2 = underTest.getCurrentLogLevel()

        // Check
        verify(exactly = 0) {
            context.getSharedPreferences(any(), any())
            sp.getString(any(), any())
            spEditor.putString(any(), any())
        }

        assertThat(mode, equalTo(LogLevel.VERBOSE))
        assertThat(mode2, equalTo(LogLevel.VERBOSE))
    }

    @Test
    fun `Если значения в sharedPrefs не было, то по умолчанию возьмется режим INFO`() {
        // Prepare
        every { context.packageName } returns "com.test.name"
        every { sp.getString(any(), any()) } returns null

        // Do
        val mode = underTest.getCurrentLogLevel()

        // Check
        assertThat(mode, equalTo(LogLevel.INFO))
        verify { sp.getString("com.test.name", null) }
    }

    @Test
    fun `Если значения в sharedPrefs было, то оно запросится 1 раз`() {
        // Prepare
        every { context.packageName } returns "com.test.name"
        every { sp.getString(any(), any()) } returns "VERBOSE"

        // Do
        val mode = underTest.getCurrentLogLevel()
        val mode2 = underTest.getCurrentLogLevel()

        // Check
        assertThat(mode, equalTo(LogLevel.VERBOSE))
        assertThat(mode2, equalTo(LogLevel.VERBOSE))
        verify(exactly = 1) { sp.getString("com.test.name", null) }
    }

    @Test
    fun `Если выставить значение в репозиторий, оно сохранится в SharedPrefs`() {
        // Prepare
        every { context.packageName } returns "com.test.name"

        // Do
        underTest.setCurrentLogLevel(LogLevel.NONE)

        // Check
        verify {
            spEditor.putString("com.test.name", "NONE")
            spEditor.commit()
        }
    }

    @Test
    fun `Если ранее выставили значение, то следующий запрос не будет считываться из SharedPrefs`() {
        // Prepare

        // Do
        underTest.setCurrentLogLevel(LogLevel.ERROR)
        val nextMode = underTest.getCurrentLogLevel()

        // Check
        verify(inverse = true) {
            sp.getString(any(), any())
        }

        assertThat(nextMode, equalTo(LogLevel.ERROR))
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            mockkStatic(Log::class)
            every { Log.d(any(), any()) } returns 1
        }

        @JvmStatic
        @AfterClass
        fun cleanUp() {
            unmockkStatic(Log::class)
        }
    }
}
