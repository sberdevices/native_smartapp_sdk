package ru.sberdevices.services.appstate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import ru.sberdevices.common.assert.Asserts
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.appstate.AppStateProvider
import ru.sberdevices.services.appstate.AppStateRequestManager

private const val SERVICE_APP_ID = "ru.sberdevices.services"
private const val SERVICE_NAME = "ru.sberdevices.services.appstate.AppStateService"

@AnyThread
internal class AppStateManagerImpl(private val appContext: Context) : AppStateRequestManager {

    private val logger = Logger.get("AppStateManagerImpl")

    private val handlerThread = HandlerThread("app_state_manager_thread").apply { start() }
    private val handler = Handler(handlerThread.looper)

    private val monitor = Object()

    @GuardedBy("monitor")
    private var service: IAppStateService? = null

    @GuardedBy("monitor")
    private var provider: AppStateProvider? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        @MainThread
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            logger.debug { "onServiceConnected(className=$className" }

            synchronized(monitor) {
                service = IAppStateService.Stub.asInterface(binder)
                handler.post { execute { service -> service.setProvider(providerInternal) } }
                monitor.notifyAll()
            }
        }

        @MainThread
        override fun onServiceDisconnected(componentName: ComponentName) {
            logger.debug { "onServiceDisconnected()" }
            synchronized(monitor) { service = null }
        }

        @MainThread
        override fun onBindingDied(name: ComponentName?) {
            logger.debug { "onBindingDied()" }
            connect()
        }
    }

    private val providerInternal = object : IAppStateProvider.Stub() {
        override fun getAppState(): String? {
            logger.debug { "getAppState()" }
            val provider = synchronized(monitor) { this@AppStateManagerImpl.provider }
            return provider?.getState()
        }
    }

    override fun setProvider(provider: AppStateProvider?) = synchronized(monitor) {
        this.provider = provider
    }

    init {
        logger.info { "init()" }
        connect()
    }

    private fun connect() {
        logger.info { "connect()" }

        val intent = Intent()
        intent.component = ComponentName(SERVICE_APP_ID, SERVICE_NAME)
        val result = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        logger.info { "bindService: $result" }
    }

    override fun dispose() {
        logger.info { "dispose()" }

        appContext.unbindService(connection)

        synchronized(monitor) { service = null }
        handlerThread.quitSafely()

        AppStateManagerFactory.onAppStateManagerDispose()
    }

    @WorkerThread
    private fun <T> execute(callable: (service: IAppStateService) -> T): T {
        Asserts.assertWorkerThread()

        var result: T

        while (true) {
            val service = waitForService()
            try {
                result = callable.invoke(service)
                break
            } catch (exception: DeadObjectException) {
                synchronized(monitor) { this@AppStateManagerImpl.service = null }
                logger.warn { "The object we are calling has died, because its hosting process no longer exists. Retrying..." }
                // We just want to wait for ServiceConnection#onServiceConnected(...)
            }
        }

        return result
    }

    @WorkerThread
    private fun waitForService(): IAppStateService {
        logger.debug { "waitForService()" }
        Asserts.assertWorkerThread()

        return synchronized(monitor) {
            var service = this@AppStateManagerImpl.service
            while (service == null) {
                try {
                    monitor.wait()
                } catch (exception: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw RuntimeException("Thread interrupted, execution can not be continued", exception)
                }
                service = this@AppStateManagerImpl.service
            }
            logger.debug { "waitForService() completed" }
            service
        }
    }
}
