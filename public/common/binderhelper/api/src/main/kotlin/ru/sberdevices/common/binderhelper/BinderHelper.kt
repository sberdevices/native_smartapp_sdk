package ru.sberdevices.common.binderhelper

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.IInterface
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.StateFlow
import ru.sberdevices.common.binderhelper.entities.BinderState
import ru.sberdevices.common.binderhelper.entities.BinderException

/**
 * Интерфейс для подключения к aidl сервисам. Имплементацию нужно получать в BinderHelperFactory.
 * На корутинах, без блокирования потоков и лишних переключений контекста. Заменяет deprecated BinderHelper.
 * Для примера использования - см. UserSettingsManagerImpl.
 */
interface BinderHelper<BinderInterface : IInterface> {

    /**
     * Флоу текущего состояния соединения с сервисом. По умолчанию [BinderState.DISCONNECTED]
     */
    val binderStateFlow: StateFlow<BinderState>

    /**
     * Проверяем наличие сервиса
     */
    fun hasService(): Boolean

    /**
     * Асинхронно подключаемся к сервису. Если сразу подключиться не удалось, но сервис есть на
     * девайсе, пытаемся сделать это бесконечно раз в секунду, пока корутину не отменят.
     * @return true - если процесс старта сервиса был начат и были пройдены все проверки на пермишены
     */
    fun connect(): Boolean

    /**
     * Отписываемся от сервиса, если не получили новый объект биндера
     */
    fun disconnect()

    /**
     * Ждем подключения к сервису и пытаемся исполнить aidl метод.
     * Если получили [DeadObjectException], то чистим биндер и уходим в suspend,
     * пока не подключимся заново через [connect]. В этом случае,
     * мы должны также получить onBindingDied() в [ServiceConnection], который как раз и вызовет [connect].
     *
     * В случае если контекст, в котором выполняемся отменили - вернет null.
     */
    suspend fun <T> execute(method: (binder: BinderInterface) -> T?): T?

    /**
     * Использовать аналогично [execute] методу, но только тогда, когда ожидаемый IPC ответ != null.
     *
     * @return [T], обернутый в [Result], где:
     * - [Result.success] имеет non-null значение
     * - [Result.failure] содержит одно из [BinderException] исключений, в том числе - получение null значение через IPC
     */
    suspend fun <T> executeWithResult(method: (binder: BinderInterface) -> T): Result<T>

    /**
     * Пытаемся выполнить aidl-метод, если есть активное соединение.
     * Если соединения нет, то просто чистим биндер и возвращаем null.
     * Удобно использовать для очистки там, где нет suspend-контекста,
     * например в awaitClose {} в callbackFlow.
     */
    @WorkerThread
    fun <T> tryExecute(method: (binder: BinderInterface) -> T?): T?

    /**
     * Использовать аналогично [tryExecute] методу, но только тогда, когда ожидаемый IPC ответ != null.
     *
     * @return [T], обернутый в [Result], где:
     * - [Result.success] имеет non-null значение
     * - [Result.failure] содержит одно из [BinderException] исключений, в том числе - получение null значение через IPC
     */
    @WorkerThread
    fun <T> tryExecuteWithResult(method: (binder: BinderInterface) -> T?): Result<T>

    /**
     * Аналогично [execute] методу, но здесь [method] передается в виде suspend лямбды.
     */
    suspend fun <T> suspendExecute(method: suspend (binder: BinderInterface) -> T?): T?

    /**
     * Использовать аналогично [suspendExecute] методу, но только тогда, когда ожидаемый IPC ответ != null.
     *
     * @return [T], обернутый в [Result], где:
     * - [Result.success] имеет non-null значение
     * - [Result.failure] содержит одно из [BinderException] исключений, в том числе - получение null значение через IPC
     */
    suspend fun <T> suspendExecuteWithResult(method: suspend (binder: BinderInterface) -> T?): Result<T>

    companion object {
        /**
         * Создает интент для подключения к компоненту с указанным именем пакета и именем класса
         */
        fun createBindIntent(packageName: String, className: String) = Intent().apply {
            component = ComponentName(packageName, className)
        }
    }
}
