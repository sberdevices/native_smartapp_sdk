@file:Suppress("NonAsciiCharacters")

package ru.sberdevices.common.logger

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.sberdevices.common.logger.mode.LogLevel

/**
 * Тесты для [AndroidLoggerDelegate]
 * @author Николай Пахомов on 04.08.2021
 */
@RunWith(Parameterized::class)
class AndroidLoggerDelegateTest(
    private val level: Level,
    private val exactly: Int,
    private val isDebug: Boolean,
    private val logLevel: LogLevel,
) {

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.v(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun cleanUp() {
        unmockkStatic(Log::class)
    }

    @Test
    fun test() {
        val prefix = "TEST/"

        val underTest = AndroidLoggerDelegate(
            allowLogSensitive = false,
            isDebugBuild = isDebug,
            logLevel = { logLevel },
            prefix = prefix
        )

        when (level) {
            Level.V -> underTest.verbose(TAG) { MESSAGE }
            Level.D -> underTest.debug(TAG) { MESSAGE }
            Level.I -> underTest.info(TAG) { MESSAGE }
            Level.W -> underTest.warn(TAG) { MESSAGE }
            Level.E -> underTest.error(TAG) { MESSAGE }
        }

        when (level) {
            Level.V -> verify(exactly = exactly) { Log.v(prefix + TAG, MESSAGE) }
            Level.D -> verify(exactly = exactly) { Log.d(prefix + TAG, MESSAGE) }
            Level.I -> verify(exactly = exactly) { Log.i(prefix + TAG, MESSAGE) }
            Level.W -> verify(exactly = exactly) { Log.w(prefix + TAG, MESSAGE) }
            Level.E -> verify(exactly = exactly) { Log.e(prefix + TAG, MESSAGE) }
        }
    }

    companion object {
        private const val TAG = "tag"
        private const val MESSAGE = "test_message"

        enum class Level {
            V, D, I, W, E
        }

        data class Params(
            private val level: Level,
            private val exactly: Int,
            private val isDebug: Boolean,
            private val logLevel: LogLevel,
        ) {
            fun toArray() = arrayOf(level, exactly, isDebug, logLevel)
        }

        @JvmStatic
        @Parameterized.Parameters(name = "level {0}, exactly {1}, isDebug {2}, logLevel: {3}")
        fun parameters() = listOf(
            Params(Level.V, exactly = 1, isDebug = true, LogLevel.VERBOSE).toArray(),
            Params(Level.D, exactly = 1, isDebug = true, LogLevel.VERBOSE).toArray(),
            Params(Level.I, exactly = 1, isDebug = true, LogLevel.VERBOSE).toArray(),
            Params(Level.W, exactly = 1, isDebug = true, LogLevel.VERBOSE).toArray(),
            Params(Level.E, exactly = 1, isDebug = true, LogLevel.VERBOSE).toArray(),

            Params(Level.V, exactly = 1, isDebug = false, LogLevel.VERBOSE).toArray(),
            Params(Level.D, exactly = 1, isDebug = false, LogLevel.VERBOSE).toArray(),
            Params(Level.I, exactly = 1, isDebug = false, LogLevel.VERBOSE).toArray(),
            Params(Level.W, exactly = 1, isDebug = false, LogLevel.VERBOSE).toArray(),
            Params(Level.E, exactly = 1, isDebug = false, LogLevel.VERBOSE).toArray(),

            Params(Level.V, exactly = 1, isDebug = true, LogLevel.DEBUG).toArray(),
            Params(Level.D, exactly = 1, isDebug = true, LogLevel.DEBUG).toArray(),
            Params(Level.I, exactly = 1, isDebug = true, LogLevel.DEBUG).toArray(),
            Params(Level.W, exactly = 1, isDebug = true, LogLevel.DEBUG).toArray(),
            Params(Level.E, exactly = 1, isDebug = true, LogLevel.DEBUG).toArray(),

            Params(Level.V, exactly = 0, isDebug = false, LogLevel.DEBUG).toArray(),
            Params(Level.D, exactly = 1, isDebug = false, LogLevel.DEBUG).toArray(),
            Params(Level.I, exactly = 1, isDebug = false, LogLevel.DEBUG).toArray(),
            Params(Level.W, exactly = 1, isDebug = false, LogLevel.DEBUG).toArray(),
            Params(Level.E, exactly = 1, isDebug = false, LogLevel.DEBUG).toArray(),

            Params(Level.V, exactly = 1, isDebug = true, LogLevel.INFO).toArray(),
            Params(Level.D, exactly = 1, isDebug = true, LogLevel.INFO).toArray(),
            Params(Level.I, exactly = 1, isDebug = true, LogLevel.INFO).toArray(),
            Params(Level.W, exactly = 1, isDebug = true, LogLevel.INFO).toArray(),
            Params(Level.E, exactly = 1, isDebug = true, LogLevel.INFO).toArray(),

            Params(Level.V, exactly = 0, isDebug = false, LogLevel.INFO).toArray(),
            Params(Level.D, exactly = 0, isDebug = false, LogLevel.INFO).toArray(),
            Params(Level.I, exactly = 1, isDebug = false, LogLevel.INFO).toArray(),
            Params(Level.W, exactly = 1, isDebug = false, LogLevel.INFO).toArray(),
            Params(Level.E, exactly = 1, isDebug = false, LogLevel.INFO).toArray(),

            Params(Level.V, exactly = 1, isDebug = true, LogLevel.WARN).toArray(),
            Params(Level.D, exactly = 1, isDebug = true, LogLevel.WARN).toArray(),
            Params(Level.I, exactly = 1, isDebug = true, LogLevel.WARN).toArray(),
            Params(Level.W, exactly = 1, isDebug = true, LogLevel.WARN).toArray(),
            Params(Level.E, exactly = 1, isDebug = true, LogLevel.WARN).toArray(),

            Params(Level.V, exactly = 0, isDebug = false, LogLevel.WARN).toArray(),
            Params(Level.D, exactly = 0, isDebug = false, LogLevel.WARN).toArray(),
            Params(Level.I, exactly = 0, isDebug = false, LogLevel.WARN).toArray(),
            Params(Level.W, exactly = 1, isDebug = false, LogLevel.WARN).toArray(),
            Params(Level.E, exactly = 1, isDebug = false, LogLevel.WARN).toArray(),

            Params(Level.V, exactly = 1, isDebug = true, LogLevel.ERROR).toArray(),
            Params(Level.D, exactly = 1, isDebug = true, LogLevel.ERROR).toArray(),
            Params(Level.I, exactly = 1, isDebug = true, LogLevel.ERROR).toArray(),
            Params(Level.W, exactly = 1, isDebug = true, LogLevel.ERROR).toArray(),
            Params(Level.E, exactly = 1, isDebug = true, LogLevel.ERROR).toArray(),

            Params(Level.V, exactly = 0, isDebug = false, LogLevel.ERROR).toArray(),
            Params(Level.D, exactly = 0, isDebug = false, LogLevel.ERROR).toArray(),
            Params(Level.I, exactly = 0, isDebug = false, LogLevel.ERROR).toArray(),
            Params(Level.W, exactly = 0, isDebug = false, LogLevel.ERROR).toArray(),
            Params(Level.E, exactly = 1, isDebug = false, LogLevel.ERROR).toArray(),

            Params(Level.V, exactly = 1, isDebug = true, LogLevel.NONE).toArray(),
            Params(Level.D, exactly = 1, isDebug = true, LogLevel.NONE).toArray(),
            Params(Level.I, exactly = 1, isDebug = true, LogLevel.NONE).toArray(),
            Params(Level.W, exactly = 1, isDebug = true, LogLevel.NONE).toArray(),
            Params(Level.E, exactly = 1, isDebug = true, LogLevel.NONE).toArray(),

            Params(Level.V, exactly = 0, isDebug = false, LogLevel.NONE).toArray(),
            Params(Level.D, exactly = 0, isDebug = false, LogLevel.NONE).toArray(),
            Params(Level.I, exactly = 0, isDebug = false, LogLevel.NONE).toArray(),
            Params(Level.W, exactly = 0, isDebug = false, LogLevel.NONE).toArray(),
            Params(Level.E, exactly = 0, isDebug = false, LogLevel.NONE).toArray(),
        )
    }
}
