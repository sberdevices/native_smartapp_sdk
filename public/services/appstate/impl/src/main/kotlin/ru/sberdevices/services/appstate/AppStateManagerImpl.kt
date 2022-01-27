package ru.sberdevices.services.appstate

import androidx.annotation.BinderThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.sberdevices.common.binderhelper.BinderHelperFactory
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.common.coroutines.CoroutineDispatchers
import ru.sberdevices.services.appstate.entities.AppStateServiceStatus
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal class AppStateManagerImpl(
    binderHelperFactory: BinderHelperFactory<IAppStateService>,
    coroutineDispatchers: CoroutineDispatchers
) : AppStateRequestManager {

    private val logger = Logger.get("AppStateManagerImpl")

    private val scope = CoroutineScope(SupervisorJob() + coroutineDispatchers.default)

    private val providerReference = AtomicReference<AppStateProvider>(null)

    private val rwLock = ReentrantReadWriteLock()

    private val backgroundAppProviders = mutableMapOf<String, IAppStateProvider.Stub?>()

    private val providerInternal = object : IAppStateProvider.Stub() {
        @BinderThread
        override fun getAppState(): String? {
            logger.debug { "getAppState()" }
            val provider = providerReference.get()
            return provider?.getState()
        }
    }

    private val helper = binderHelperFactory.create()

    override val appStateServiceStatusFlow: StateFlow<AppStateServiceStatus> = callbackFlow {

        val appStateStatusListener = object : IAppStateStatusListener.Stub() {
            override fun onAppStateConnected() {
                logger.debug { "onAppStateConnected()" }
                if (!isClosedForSend) {
                    trySend(AppStateServiceStatus.READY)
                }
            }
        }

        logger.debug { "registering AppStateStatusListener" }
        helper.execute { it.addAppStateStatusListener(appStateStatusListener) }

        awaitClose {
            logger.debug { "awaitClose, removing AppStateStatusListener" }
            helper.tryExecute { it.removeAppStateStatusListener(appStateStatusListener) }
        }
    }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppStateServiceStatus.UNREADY
        )

    init {
        logger.info { "init" }

        helper.connect()
        scope.launch {
            helper.execute { service -> service.setProvider(providerInternal) }
        }
    }

    override fun setProvider(provider: AppStateProvider?) {
        logger.debug { "setProvider: $provider" }
        providerReference.set(provider)
    }

    override fun setProvider(androidApplicationID: String, provider: AppStateProvider?) {
        if (provider != null) {
            logger.debug { "registering provider for: $androidApplicationID" }
            rwLock.write {
                backgroundAppProviders[androidApplicationID] = object : IAppStateProvider.Stub() {
                    @BinderThread
                    override fun getAppState(): String? {
                        logger.debug { "getAppState()" }
                        return provider.getState()
                    }
                }
            }
        } else {
            logger.debug { "removing provider for: $androidApplicationID" }
            rwLock.write {
                backgroundAppProviders.remove(androidApplicationID)
            }
        }

        scope.launch {
            helper.execute { service ->
                logger.debug { "setting provider for: $androidApplicationID" }
                rwLock.read {
                    service.setProviderForApp(
                        backgroundAppProviders.getOrDefault(androidApplicationID, null),
                        androidApplicationID
                    )
                }
            }
        }
    }

    override fun registerBackgroundApp(packageName: String) {
        logger.debug { "registerBackgroundApp: $packageName" }

        scope.launch {
            helper.execute { it.registerBackgroundApp(packageName) }
        }
    }

    override fun unregisterBackgroundApp(packageName: String) {
        logger.debug { "unregisterBackgroundApp: $packageName" }

        scope.launch {
            helper.execute { it.unregisterBackgroundApp(packageName) }
        }
    }

    override fun dispose() {
        logger.info { "dispose()" }

        scope.launch {
            rwLock.read {
                logger.debug { "clearing previously set providers, size: ${backgroundAppProviders.size}" }
                backgroundAppProviders.forEach { (packageName, _) ->
                    helper.execute { it.setProviderForApp(null, packageName) }
                }
            }
            rwLock.write {
                backgroundAppProviders.clear()
            }
            helper.disconnect()
            scope.cancel()
        }

        AppStateManagerFactory.onAppStateManagerDispose()
    }
}
