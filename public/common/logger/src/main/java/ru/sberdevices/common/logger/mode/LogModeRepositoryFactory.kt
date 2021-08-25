package ru.sberdevices.common.logger.mode

import android.content.Context

/**
 * @author Николай Пахомов on 04.08.2021
 */
object LogModeRepositoryFactory {

    /**
     * Заполучить [Lazy] холдер инстанса [LogLevelRepository].
     * Лениво, т.к. на этапе init у [Application] контекст еще не создан.
     */
    fun createLazy(
        context: Context,
        isDebugBuild: Boolean
    ): Lazy<LogLevelRepository> = lazy {
        LogLevelRepositoryImpl(
            context = context,
            isDebugBuild = isDebugBuild
        )
    }
}
