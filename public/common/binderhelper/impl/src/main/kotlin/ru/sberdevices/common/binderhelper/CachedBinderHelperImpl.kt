package ru.sberdevices.common.binderhelper

import android.os.IInterface
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.sberdevices.common.binderhelper.entities.BinderException
import ru.sberdevices.common.logger.Logger
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Обертка над [BinderHelper], которая кеширует соединения с сервисом.
 * Позволяет делать независимые connect/disconnect для разных процессов,
 * которые в реальности работают через одно, закешированное соединение
 *
 * Для выполнения aidl метода достаточно просто вызывать метод [execute] без вызова [connect].
 * Внутри автоматически выполнится [connect], если соединения с сервисом еще не было.
 * Если нет других процессов, использующих соединение с сервисом, то по завершению метода [execute] автоматически
 * выполнится [disconnect] с заданной задержкой
 *
 * В случае явного вызова [connect], соединение с сервисом будет поддерживаться до явного вызова [disconnect]
 * Так как внутри используется счетчик соединений, то каждый явный вызов [connect] должен сопровождаться
 * явным вызовом [disconnect]
 */
internal class CachedBinderHelperImpl<BinderInterface : IInterface>(
    private val helper: BinderHelper<BinderInterface>,
    private val logger: Logger,
    private val disconnectDelay: Long
) : CachedBinderHelper<BinderInterface>, BinderHelper<BinderInterface> by helper {

    private val scope = CoroutineScope(SupervisorJob())
    private val connectCounter = AtomicInteger(0)
    private var disconnectJobRef = AtomicReference<Job?>(null)

    /**
     * Признак, что есть активное соединение
     */
    @Volatile
    override var hasConnection: Boolean = false
        private set

    override val connectionCount: Int
        get() = connectCounter.get()

    @VisibleForTesting
    val hasScheduleDisconnectTask: Boolean
        get() = disconnectJobRef.get()?.isActive ?: false

    override fun connect(): Boolean {
        val connectionCount = connectCounter.incrementAndGet()
        logger.debug { "cachedConnect(), connectionCount == $connectionCount" }

        cancelDisconnectTask()
        if (!hasConnection) {
            binderConnect()
        }

        return hasConnection
    }

    override fun disconnect() {
        val connectionCount = connectCounter.decrementAndGet()
        logger.debug { "cachedDisconnect(), connectionCount == $connectionCount" }

        if (connectionCount < 0) {
            throw IllegalStateException("service already disconnected, connection counter value < 0")
        }
        if (connectionCount == 0) {
            // Если соединений больше нет, то создаем задание на отложенный disconnect
            scheduleDisconnectTask()
        }
    }

    override suspend fun <Result> execute(method: (binder: BinderInterface) -> Result?): Result? = suspendExecute {
        method(it)
    }

    override suspend fun <T> executeWithResult(method: (binder: BinderInterface) -> T): Result<T> =
        suspendExecuteWithResult { method(it) }

    override suspend fun <Result> suspendExecute(method: suspend (binder: BinderInterface) -> Result?): Result? {
        return if (connect()) {
            try {
                helper.suspendExecute(method)
            } finally {
                disconnect()
            }
        } else {
            null
        }
    }

    override suspend fun <T> suspendExecuteWithResult(method: suspend (binder: BinderInterface) -> T?): Result<T> =
        runCatching {
            if (connect()) {
                try {
                    helper.suspendExecuteWithResult(method).getOrThrow()
                } finally {
                    disconnect()
                }
            } else {
                throw BinderException.ConnectionNotEstablished()
            }
        }

    @Synchronized
    private fun binderConnect() {
        if (!hasConnection) {
            logger.debug { "binderConnect()" }
            hasConnection = helper.connect()
            logger.debug { "binder.isConnected == $hasConnection" }
        }
    }

    @Synchronized
    private fun binderDisconnect() {
        cancelDisconnectTask()
        if (connectCounter.get() == 0) {
            logger.debug { "binderDisconnect()" }
            helper.disconnect()
            hasConnection = false
            logger.debug { "binder.isConnected == $hasConnection" }
        }
    }

    /**
     * Создает отложенное задание на реальный [disconnect]
     */
    private fun scheduleDisconnectTask() {
        setDisconnectJob(
            scope.launch {
                delay(disconnectDelay)
                if (isActive) {
                    binderDisconnect()
                }
            }
        )
    }

    /**
     * Удаляет отложенное задание на реальный [disconnect]
     */
    private fun cancelDisconnectTask() {
        setDisconnectJob(null)
    }

    private fun setDisconnectJob(newJob: Job?) {
        val oldJob = disconnectJobRef.getAndSet(newJob)
        if (oldJob != null && oldJob.isActive) {
            oldJob.cancel()
        }
    }
}
