package ru.sberdevices.common.binderhelper

import android.os.IInterface
import androidx.annotation.BinderThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.sberdevices.common.logger.Logger
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Обертка над [BinderHelper], которая кеширует соединения с сервисом.
 * Позволяет делать независимые connect/disconnect для разных процессов,
 * которые в реальности работают через одно, закешированное соединение
 *
 * Внутри хранит счетчик соединений, [connect] инкрементирует счетчик, [disconnect] декрементирует
 * Реальное соединения и рассоединения происходит, когда счетчик = 0
 *
 * Метод [execute] внутри делает свой connect, выполняет тело метода и затем делает disconnect
 *
 * Реальный [disconnect] происходит с задержкой.
 * Это позволяет последовательно вызывать несколько [execute] в рамках одного физического соединения
 *
 * @author Максим Сидоров on 11.05.2021.
 */
internal class CachedBinderHelper<BinderInterface : IInterface>(
    private val helper: BinderHelper<BinderInterface>,
    private val logger: Logger,
    private val disconnectDelay: Long
) : BinderHelper<BinderInterface> by helper {

    private val scope = CoroutineScope(SupervisorJob())
    private val connectCounter = AtomicInteger(0)
    private var disconnectJobRef = AtomicReference<Job?>(null)

    /**
     * Признак, что есть активное соединение
     */
    @Volatile
    var hasConnection: Boolean = false
        private set

    @VisibleForTesting
    val connectionCount: Int get() = connectCounter.get()
    @VisibleForTesting
    val hasScheduleDisconnectTask: Boolean get() = disconnectJobRef.get()?.isActive ?: false

    override fun connect(): Boolean {
        cancelDisconnectTask()
        if (!hasConnection) {
            binderConnect()
        }

        val connectionCount = connectCounter.incrementAndGet()
        logger.debug { "cachedConnect(), connectionCount == $connectionCount" }
        return hasConnection
    }

    override fun disconnect() {
        val connectionCount = connectCounter.decrementAndGet()
        if (connectionCount < 0) {
            throw IllegalStateException("service already disconnected, connection counter value < 0")
        }
        if (connectionCount == 0) {
            // Если соединений больше нет, то создаем задание на отложенный disconnect
            scheduleDisconnectTask()
        }
        logger.debug { "cachedDisconnect(), connectionCount == $connectionCount" }
    }

    @BinderThread
    override suspend fun <Result> execute(method: (binder: BinderInterface) -> Result): Result? {
        return if (connect()) {
            try {
                helper.execute(method)
            } finally {
                disconnect()
            }
        } else {
            null
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
