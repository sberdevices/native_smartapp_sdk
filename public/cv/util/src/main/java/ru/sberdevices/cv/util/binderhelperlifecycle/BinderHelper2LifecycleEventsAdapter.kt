package ru.sberdevices.cv.util.binderhelperlifecycle

import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.sberdevices.common.binderhelper.BinderHelper2
import ru.sberdevices.common.binderhelper.BinderHelper2Factory
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.cv.util.binderhelperlifecycle.entity.BinderLifecycleEvent

/**
 * Gives [BinderHelper2] opportunity to observe ServiceConnection lifecycle events
 */

class BinderHelper2LifecycleEventsAdapter<BinderInterface : Any>(
    context: Context,
    targetIntent: Intent,
    binderHelperFactory: BinderHelper2Factory = BinderHelper2Factory,
    private val getBinding: (IBinder) -> BinderInterface,
) : EventPublisher<BinderLifecycleEvent>, BinderHelper2<BinderInterface> {

    private val logger by Logger.lazy(tag = javaClass.simpleName)

    private val coroutineScope = CoroutineScope(
        SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
            logger.error(throwable) { "coroutine $coroutineContext exception" }
        }
    )

    private val _binderLifecycleEvents = MutableSharedFlow<BinderLifecycleEvent>(replay = 1)

    override val events: Flow<BinderLifecycleEvent> = _binderLifecycleEvents.asSharedFlow()

    private val binderHelper = binderHelperFactory.getBinderHelper2(
        context = context,
        intent = targetIntent,
        getBinding = { binder ->
            coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.CONNECTED) }
            getBinding(binder)
        },
        onDisconnect = { coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.DISCONNECTED) } },
        onBindingDied = { coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.BINDING_DIED) } },
        onNullBinding = { coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.NULL_BINDING) } }
    )

    override fun connect(): Boolean {
        return binderHelper.connect()
    }

    override fun disconnect() {
        binderHelper.disconnect()
        _binderLifecycleEvents.resetReplayCache()
        coroutineScope.cancel()
    }

    override suspend fun <Result> execute(method: (binder: BinderInterface) -> Result): Result? {
        return binderHelper.execute(method)
    }

    override fun <Result> tryExecute(method: (binder: BinderInterface) -> Result): Result? {
        return binderHelper.tryExecute(method)
    }
}
