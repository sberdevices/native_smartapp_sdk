package ru.sberdevices.cv.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.sberdevices.common.logger.Logger

interface BindingIdStorage {
    val bindingId: StateFlow<Int?>
    fun set(bindingId: Int)
    fun get(): Int?
}

class BindingIdStorageImpl : BindingIdStorage {

    private val logger by Logger.lazy(javaClass.simpleName)

    override val bindingId = MutableStateFlow<Int?>(null)

    override fun set(bindingId: Int) {
        val updated = this.bindingId.compareAndSet(expect = null, update = bindingId)
        logger.debug { "Binding id ${if (updated) "" else "not "}updated to $bindingId" }
    }

    override fun get(): Int? {
        return this.bindingId.value
    }
}
