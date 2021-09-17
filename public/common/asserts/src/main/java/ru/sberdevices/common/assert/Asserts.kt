package ru.sberdevices.common.assert

import android.os.Looper
import androidx.annotation.AnyThread

@AnyThread
object Asserts {

    /**
     * Asserts must be enabled for debug builds but disabled for production.
     */
    @Volatile
    var enabled = false

    fun assertTrue(value: Boolean?, message: String? = null) {
        if (enabled) {
            if (value != true) {
                throw AssertionError(message)
            }
        }
    }

    fun assertFalse(value: Boolean?, message: String? = null) {
        if (enabled) {
            if (value != false) {
                throw AssertionError(message)
            }
        }
    }

    fun assertMainThread(message: String? = null) {
        if (enabled) {
            if (!Looper.getMainLooper().isCurrentThread) {
                throw AssertionError(message)
            }
        }
    }

    fun assertWorkerThread(message: String? = null) {
        if (enabled) {
            if (Looper.getMainLooper().isCurrentThread) {
                throw AssertionError(message)
            }
        }
    }

    fun assertNonNull(value: Any?, message: String? = null) {
        if (enabled) {
            if (value == null) {
                throw AssertionError(message)
            }
        }
    }

    fun fail(message: String? = null) {
        if (enabled) {
            throw AssertionError(message)
        }
    }
}
