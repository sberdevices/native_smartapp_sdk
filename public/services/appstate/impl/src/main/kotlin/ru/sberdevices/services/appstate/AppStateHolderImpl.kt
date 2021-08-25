package ru.sberdevices.services.appstate

import android.content.Context
import androidx.annotation.AnyThread

@AnyThread
internal class AppStateHolderImpl(appContext: Context) : AppStateHolder {

    private val stateManager = AppStateManagerFactory.createRequestManager(appContext)

    private val provider = object : AppStateProvider {
        override fun getState() = appState
    }

    @Volatile
    private var appState: String? = null

    init {
        stateManager.setProvider(provider)
    }

    override fun dispose() {
        stateManager.dispose()
    }

    override fun setState(state: String?) {
        appState = state
    }
}
