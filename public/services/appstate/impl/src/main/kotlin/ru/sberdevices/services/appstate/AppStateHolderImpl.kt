package ru.sberdevices.services.appstate

import android.content.Context
import androidx.annotation.AnyThread
import ru.sberdevices.common.coroutines.CoroutineDispatchers

@AnyThread
internal class AppStateHolderImpl(appContext: Context, coroutineDispatchers: CoroutineDispatchers) : AppStateHolder {

    private val stateManager = AppStateManagerFactory.createRequestManager(appContext, coroutineDispatchers)

    private val provider = AppStateProvider { appState }

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
