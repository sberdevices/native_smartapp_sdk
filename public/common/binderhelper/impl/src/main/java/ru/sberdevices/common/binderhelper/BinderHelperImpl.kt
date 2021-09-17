package ru.sberdevices.common.binderhelper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.DeadObjectException
import android.os.IBinder
import android.os.IInterface
import androidx.annotation.BinderThread
import androidx.annotation.MainThread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import ru.sberdevices.common.binderhelper.entities.BinderState
import ru.sberdevices.common.logger.Logger

/**
 * Новый хелпер для подключения к сервисам, полностью на корутинах,
 * без блокирования потоков и лишних переключений контекста.
 *
 * Принимаемые на вход колбеки срабатывают, если активен скоуп, в котором подключаемся к сервису.
 */
internal class BinderHelperImpl<BinderInterface : IInterface>(
    private val context: Context,
    private val intent: Intent,
    private val logger: Logger,
    private val getBinding: (IBinder) -> BinderInterface,
) : BinderHelper<BinderInterface> {

    private val binderState = MutableStateFlow<BinderInterface?>(null)
    private val connectionState = MutableStateFlow<ServiceConnection?>(null)

    private val mutableBinderStateFlow = MutableStateFlow(BinderState.DISCONNECTED)

    override val binderStateFlow: StateFlow<BinderState>
        get() = mutableBinderStateFlow

    private fun getConnection(): ServiceConnection = object : ServiceConnection {
        @MainThread
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            logger.debug { "onServiceConnected(className=$className" }
            binderState.value = getBinding(iBinder)

            mutableBinderStateFlow.value = BinderState.CONNECTED
        }

        @MainThread
        override fun onServiceDisconnected(componentName: ComponentName) {
            logger.debug { "onServiceDisconnected(className=$componentName)" }
            clearBinder()
            mutableBinderStateFlow.value = BinderState.DISCONNECTED
        }

        @MainThread
        override fun onBindingDied(name: ComponentName?) {
            logger.debug { "onBindingDied()" }
            mutableBinderStateFlow.value = BinderState.BINDING_DIED
            connect()
        }

        @MainThread
        override fun onNullBinding(name: ComponentName?) {
            logger.debug { "onNullBinding()" }
            mutableBinderStateFlow.value = BinderState.NULL_BINDING
        }
    }

    /**
     * В некоторых случаях нельзя просто обнулить биндер - мы уже теоретически можем получить новый биндер
     * поэтому атомарно проверяем, что объект биндера не изменился, и если это так - только тогда зануляем.
     */
    private fun clearBinder(binder: BinderInterface? = binderState.value) {
        binderState.compareAndSet(binder, null)
    }

    /**
     * Асинхронно подключаемся к сервису и получаем aidl-интерфейс через [getBinding].
     * Если сразу подключиться не удалось - пытаемся сделать это бесконечно раз в секунду, пока корутину не отменят.
     */
    override fun connect(): Boolean {
        logger.verbose { "try to connect() intent=${intent.component?.className}" }
        if (context.packageManager.queryIntentServices(intent, PackageManager.MATCH_ALL).isEmpty()) {
            logger.warn { "service (${intent.component}) is not present in the system, will not connect" }
            return false
        }

        val newConnection = getConnection().also { connectionState.value = it }

        val success = context.applicationContext.bindService(intent, newConnection, Context.BIND_AUTO_CREATE)
        if (success) {
            logger.debug { "got permission to connect with intent=${intent.component?.className}" }
        } else {
            logger.warn { "Failed to connect to ${intent.component?.className}" }
        }
        return success
    }

    override fun disconnect() {
        logger.info { "disconnect()" }

        clearBinder()
        connectionState.value?.let { context.applicationContext.unbindService(it) }
        connectionState.value = null
    }

    /**
     * Ждем подключения к сервису и пытаемся исполнить aidl метод.
     * Если получили [DeadObjectException], то чистим биндер и уходим в suspend,
     * пока не подключимся заново через [connect]. В этом случае,
     * мы должны также получить onBindingDied() в [ServiceConnection], который как раз и вызовет [connect].
     *
     * В случае если контекст, в котором выполняемся отменили - вернет null.
     */
    @BinderThread
    override suspend fun <Result> execute(method: (binder: BinderInterface) -> Result): Result? {
        while (currentCoroutineContext().isActive) {
            val binder = binderState
                .filterNotNull()
                .first()
            try {
                return method(binder)
            } catch (e: DeadObjectException) {
                clearBinder(binder)
                logger.warn {
                    "The object we are calling has died, because its hosting process no longer exists. Retrying..."
                }
                // We just want to wait for ServiceConnection#onServiceConnected(...)
            }
        }

        throw CancellationException("Connection is cancelled")
    }

    /**
     * Пытаемся выполнить aidl-метод, если есть активное соединение.
     * Если соединения нет, то просто чистим биндер и возвращаем null.
     * Удобно использовать для очистки там, где нет suspend-контекста,
     * например в awaitClose {} в callbackFlow.
     */
    @BinderThread
    override fun <Result> tryExecute(method: (binder: BinderInterface) -> Result): Result? {
        val binder = binderState.value
        return if (binder != null) {
            try {
                method(binder)
            } catch (e: DeadObjectException) {
                logger.warn {
                    "The object we are calling has died, because its hosting process no longer exists. Retrying..."
                }
                clearBinder(binder)
                null
            }
        } else {
            logger.info {
                "The object we are calling has died, because its hosting process no longer exists..."
            }
            null
        }
    }
}
