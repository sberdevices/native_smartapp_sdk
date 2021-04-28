package ru.sberdevices.services.appstate

import android.content.Context
import androidx.annotation.AnyThread
import ru.sberdevices.services.appstate.AppStateHolder
import ru.sberdevices.services.appstate.AppStateRequestManager
import ru.sberdevices.services.appstate.exceptions.AppStateManagerAlreadyExistsException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Factory for creating instances of AppStateManager. Note: it is prohibited to have more than one active (not disposed)
 * instance of AppStateManager.
 */
@AnyThread
object AppStateManagerFactory {

    private val counter = AtomicInteger()

    @JvmStatic
    @Synchronized
    @Throws(AppStateManagerAlreadyExistsException::class)
    fun createHolder(context: Context): AppStateHolder {
        // counter will be incremented during creation of AppStateRequestManager inside AppStateHolder

        return AppStateHolderImpl(context.applicationContext)
    }

    @JvmStatic
    @Synchronized
    @Throws(AppStateManagerAlreadyExistsException::class)
    fun createRequestManager(context: Context): AppStateRequestManager {
        incrementAndCheckCounter()

        return AppStateManagerImpl(context.applicationContext)
    }

    @Synchronized
    internal fun onAppStateManagerDispose() {
        counter.decrementAndGet()
    }

    private fun incrementAndCheckCounter() {
        if (counter.incrementAndGet() > 1) {
            throw AppStateManagerAlreadyExistsException()
        }
    }
}
