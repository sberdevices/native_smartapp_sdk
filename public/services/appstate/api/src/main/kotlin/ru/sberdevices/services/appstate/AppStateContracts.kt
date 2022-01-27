package ru.sberdevices.services.appstate

import androidx.annotation.AnyThread
import kotlinx.coroutines.flow.StateFlow
import ru.sberdevices.services.appstate.entities.AppStateServiceStatus
import kotlin.jvm.Throws

@AnyThread
interface AppStateManager {
    fun dispose()
}

/**
 * Holds state set by the user and provides it to the StarPlatform when needed.
 */
@AnyThread
interface AppStateHolder : AppStateManager {
    fun setState(state: String?)
}

/**
 * Request state through [AppStateProvider] when StarPlatform needed it
 */
@AnyThread
interface AppStateRequestManager : AppStateManager {

    /**
     * Флоу текущей готовности AppStateService.
     * Если AppStateService готов, он может регистрировать дополнительные приложения.
     */
    val appStateServiceStatusFlow: StateFlow<AppStateServiceStatus>

    /**
     * Выставить провайдера для текущего приложения.
     */
    fun setProvider(provider: AppStateProvider?)

    /**
     * Выставить провайдера для конкретного приложения.
     *
     * Только для использования внутренними приложениями SberDevices.
     * @exception SecurityException если вызывающее приложение не имеет подпись вендора или SberDevices.
     * @hide
     */
    @Throws(SecurityException::class)
    fun setProvider(androidApplicationID: String, provider: AppStateProvider?)

    /**
     * Зарегистрировать приложение как фоновое.
     *
     * Только для использования внутренними приложениями SberDevices.
     *
     * @param packageName пакет приложения
     * @exception SecurityException если вызывающее приложение не имеет подпись вендора или SberDevices.
     * @hide
     */
    @Throws(SecurityException::class)
    fun registerBackgroundApp(packageName: String)

    /**
     * Удалить приложение из списка фоновых.
     *
     * Только для использования внутренними приложениями SberDevices.
     *
     * @param packageName пакет приложения
     * @exception SecurityException если вызывающее приложение не имеет подпись вендора или SberDevices.
     * @hide
     */
    @Throws(SecurityException::class)
    fun unregisterBackgroundApp(packageName: String)
}

/**
 * Provides current application state to the StarPlatform.
 */
@AnyThread
fun interface AppStateProvider {
    fun getState(): String?
}
