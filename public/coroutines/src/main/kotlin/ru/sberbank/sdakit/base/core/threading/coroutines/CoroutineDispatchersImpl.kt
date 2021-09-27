package ru.sberbank.sdakit.base.core.threading.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class CoroutineDispatchersImpl : CoroutineDispatchers {
    private val singleThread by lazy { Executors.newSingleThreadExecutor() }

    override val sequentialWork: CoroutineDispatcher
        get() = singleThread.asCoroutineDispatcher()
}
