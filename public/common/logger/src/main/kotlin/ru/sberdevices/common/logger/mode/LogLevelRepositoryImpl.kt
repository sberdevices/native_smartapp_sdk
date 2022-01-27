package ru.sberdevices.common.logger.mode

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * @author Николай Пахомов on 03.08.2021
 */
internal class LogLevelRepositoryImpl(
    private val context: Context,
    private val isDebugBuild: Boolean
) : LogLevelRepository {

    private val sp: SharedPreferences by lazy {
        context.getSharedPreferences("LogLevelRepository", Context.MODE_PRIVATE)
    }

    private val logTag = LogLevelRepositoryImpl::class.java.simpleName

    private val logLevelFromPrefs: LogLevel by lazy {
        val storedMode: String? = sp.getString(context.packageName, null)
        storedMode.toLogLevel()
    }

    @Volatile
    private var currentLogLevel: LogLevel? = null

    override fun getCurrentLogLevel(): LogLevel {
        if (isDebugBuild) {
            // Приняли решение в дебажных билдах ВСЕГДА все логгировать.
            return LogLevel.VERBOSE
        }

        return currentLogLevel ?: logLevelFromPrefs.also {
            currentLogLevel = it
            Log.d(logTag, "first currentLogMode: $it")
        }
    }

    override fun setCurrentLogLevel(value: LogLevel) {
        if (isDebugBuild) {
            Log.d(logTag, "will not set $value in debug builds")
            return
        }

        currentLogLevel = value
        sp.edit().putString(context.packageName, value.toString()).commit()
        Log.d(logTag, "setCurrentLogMode: $value")
    }
}
