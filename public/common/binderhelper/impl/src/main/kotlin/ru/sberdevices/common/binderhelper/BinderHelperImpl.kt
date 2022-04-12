package ru.sberdevices.common.binderhelper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.DeadObjectException
import android.os.IBinder
import android.os.IInterface
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import ru.sberdevices.common.binderhelper.entities.BinderException
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
     * Проверяем наличие сервиса
     */
    @Suppress("WrongConstant", "QueryPermissionsNeeded")
    override fun hasService(): Boolean {
        return context.packageManager.queryIntentServices(intent, PackageManager.MATCH_ALL).isNotEmpty()
    }

    /**
     * Асинхронно подключаемся к сервису и получаем aidl-интерфейс через [getBinding].
     * Если сразу подключиться не удалось - пытаемся сделать это бесконечно раз в секунду, пока корутину не отменят.
     */
    override fun connect(): Boolean {
        logger.verbose { "try to connect() intent=${intent.component?.className}" }
        if (!hasService()) {
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
        mutableBinderStateFlow.value = BinderState.DISCONNECTED
    }

    /**
     * Ждем подключения к сервису и пытаемся исполнить aidl метод.
     * Если получили [DeadObjectException], то чистим биндер и уходим в suspend,
     * пока не подключимся заново через [connect]. В этом случае,
     * мы должны также получить onBindingDied() в [ServiceConnection], который как раз и вызовет [connect].
     *
     * В случае если контекст, в котором выполняемся отменили - вернет null.
     */
    override suspend fun <T> execute(method: (binder: BinderInterface) -> T?): T? = suspendExecute { method(it) }

    override suspend fun <T> executeWithResult(method: (binder: BinderInterface) -> T): Result<T> =
        suspendExecuteWithResult { method(it) }

    /**
     * Пытаемся выполнить aidl-метод, если есть активное соединение.
     * Если соединения нет, то просто чистим биндер и возвращаем null.
     * Удобно использовать для очистки там, где нет suspend-контекста,
     * например в awaitClose {} в callbackFlow.
     */
    @WorkerThread
    override fun <T> tryExecute(method: (binder: BinderInterface) -> T?): T? = tryExecuteWithResult(method).fold(
        onSuccess = { it },
        onFailure = { null }
    )

    @WorkerThread
    override fun <T> tryExecuteWithResult(method: (binder: BinderInterface) -> T?): Result<T> {
        val binder = binderState.value

        return runCatching {
            if (binder != null) {
                return@runCatching method(binder) ?: throw BinderException.ReceivedNullValue()
            } else {
                throw BinderException.ConnectionNotEstablished()
            }
        }.onFailure {
            if (it is DeadObjectException) {
                logger.warn {
                    "The object we are calling has died, because its hosting process no longer exists. Retrying..."
                }
                clearBinder(binder)
                // We just want to wait for ServiceConnection#onServiceConnected(...)
            }
        }
    }

    override suspend fun <T> suspendExecute(method: suspend (binder: BinderInterface) -> T?): T? =
        suspendExecuteWithResult(method).fold(
            onSuccess = { it },
            onFailure = {
                if (it is BinderException.CoroutineContextCancelled) {
                    throw CancellationException("Connection is cancelled")
                }
                null
            }
        )

    override suspend fun <T> suspendExecuteWithResult(method: suspend (binder: BinderInterface) -> T?): Result<T> {
        var binder: BinderInterface? = null

        return runCatching {
            while (currentCoroutineContext().isActive) {
                binder = binderState
                    .filterNotNull()
                    .first()

                return@runCatching method(binder!!) ?: throw BinderException.ReceivedNullValue()
            }

            throw BinderException.CoroutineContextCancelled()
        }.onFailure {
            if (it is DeadObjectException) {
                clearBinder(binder)
                logger.warn {
                    "The object we are calling has died, because its hosting process no longer exists. Retrying..."
                }
                // We just want to wait for ServiceConnection#onServiceConnected(...)
            }
        }
    }
}
