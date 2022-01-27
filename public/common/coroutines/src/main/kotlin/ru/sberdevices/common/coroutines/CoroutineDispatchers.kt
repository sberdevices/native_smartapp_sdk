package ru.sberdevices.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Диспатчеры для корутин, используемые в наших проектах.
 *
 * @author Илья Богданович on 03/09/2020
 */
interface CoroutineDispatchers {
    /**
     * [CoroutineDispatcher] используемый для UI-задач.
     */
    val ui: CoroutineDispatcher

    /**
     * [CoroutineDispatcher] используемый для по возможности немедленого выполнения UI-задач.
     */
    val uiImmediate: CoroutineDispatcher

    /**
     * [CoroutineDispatcher] используемый для фоновых IO-задач.
     */
    val io: CoroutineDispatcher

    /**
     * стандартный диспатчер
     */
    val default: CoroutineDispatcher

    /**
     * диспатчер для выполнения последовательной работы в фоне. Однопоточный.
     * На нем в частности работает сбор логов для LogRepo.
     */
    val sequentialWork: CoroutineDispatcher

    companion object Default : CoroutineDispatchers {

        override val default = Dispatchers.Default
        override val io = Dispatchers.IO
        override val sequentialWork by lazy { Executors.newSingleThreadExecutor().asCoroutineDispatcher() }
        override val ui = Dispatchers.Main
        override val uiImmediate = Dispatchers.Main.immediate
    }
}
