package ru.sberdevices.common.binderhelper

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.IInterface
import androidx.annotation.BinderThread

/**
 * Интерфейс для подключения к aidl сервисам. Имплементацию нужно получать в [BinderHelperFactory].
 * На корутинах, без блокирования потоков и лишних переключений контекста.
 */
interface BinderHelper<BinderInterface : IInterface> {

    /**
     * Асинхронно подключаемся к сервису. Если сразу подключиться не удалось, но сервис есть на
     * девайсе, пытаемся сделать это бесконечно раз в секунду, пока корутину не отменят.
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
    @BinderThread
    suspend fun <Result> execute(method: (binder: BinderInterface) -> Result): Result?

    /**
     * Пытаемся выполнить aidl-метод, если есть активное соединение.
     * Если соединения нет, то просто чистим биндер и возвращаем null.
     * Удобно использовать для очистки там, где нет suspend-контекста,
     * например в awaitClose {} в callbackFlow.
     */
    @BinderThread
    fun <Result> tryExecute(method: (binder: BinderInterface) -> Result): Result?

    companion object {
        /**
         * Создает интент для подключения к компоненту с указанным именем пакета и именем класса
         */
        fun createBindIntent(packageName: String, className: String) = Intent().apply {
            component = ComponentName(packageName, className)
        }
    }
}
