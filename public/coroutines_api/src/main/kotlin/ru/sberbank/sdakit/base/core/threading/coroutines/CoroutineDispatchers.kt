package ru.sberbank.sdakit.base.core.threading.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface CoroutineDispatchers {
    /**
     * [CoroutineDispatcher] используемый для UI-задач.
     */
    val ui: CoroutineDispatcher
        get() = Dispatchers.Main

    /**
     * [CoroutineDispatcher] используемый для по возможности немедленого выполнения UI-задач.
     */
    val uiImmediate: CoroutineDispatcher
        get() = Dispatchers.Main.immediate

    /**
     * [CoroutineDispatcher] используемый для фоновых IO-задач.
     */
    val io: CoroutineDispatcher
        get() = Dispatchers.IO

    /**
     * стандартный диспатчер
     */
    val default: CoroutineDispatcher
        get() = Dispatchers.Default

    /**
     * диспатчер для выполнения последовательной работы в фоне. Однопоточный.
     * На нем в частности работает сбор логов для LogRepo.
     */
    val sequentialWork: CoroutineDispatcher
}
