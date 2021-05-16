package ru.sberdevices.pub.demoapp.ui.cv.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Делегат для работы с содержимым [MutableStateFlow] как с полем.
 * Необходимо задать изначальное значение для [MutableStateFlow]!
 */
internal fun <T : Any> MutableStateFlow<T>.delegate(): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            tryEmit(value)
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): T = value
    }
}
