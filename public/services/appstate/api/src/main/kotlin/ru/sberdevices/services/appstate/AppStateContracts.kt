package ru.sberdevices.services.appstate

import androidx.annotation.AnyThread

@AnyThread
interface AppStateManager {
    fun dispose()
}

/**
 * Holds state set by the user and provides it to the StarPlatform when needed.
 */
@AnyThread
interface AppStateHolder : AppStateManager {
    fun setState(state: String?)
}

/**
 * Request state through [AppStateProvider] when StarPlatform needed it
 */
@AnyThread
interface AppStateRequestManager : AppStateManager {
    fun setProvider(provider: AppStateProvider?)
}

/**
 * Provides current application state to the StarPlatform.
 */
@AnyThread
interface AppStateProvider {
    fun getState(): String?
}
