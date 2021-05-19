package ru.sberdevices.cv.util.binderhelperlifecycle

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.sberdevices.common.binderhelper.BinderHelper
import ru.sberdevices.common.binderhelper.BinderHelperFactory
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.cv.util.binderhelperlifecycle.entity.BinderLifecycleEvent

/**
 * Gives [BinderHelper] opportunity to observe ServiceConnection lifecycle events
 */

class BinderHelperLifecycleEventsAdapter<BinderInterface : IInterface>(
    context: Context,
    targetIntent: Intent,
    private val getBinding: (IBinder) -> BinderInterface,
) : EventPublisher<BinderLifecycleEvent>, BinderHelper<BinderInterface> {

    private val logger by Logger.lazy(tag = javaClass.simpleName)

    private val coroutineScope = CoroutineScope(
        SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
            logger.error(throwable) { "coroutine $coroutineContext exception" }
        }
    )

    private val _binderLifecycleEvents = MutableSharedFlow<BinderLifecycleEvent>(replay = 1)

    override val events: Flow<BinderLifecycleEvent> = _binderLifecycleEvents.asSharedFlow()

    private val binderHelper = BinderHelperFactory(
        context = context,
        intent = targetIntent,
        getBinding = { binder ->
            coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.CONNECTED) }
            getBinding(binder)
        },
        onDisconnect = { coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.DISCONNECTED) } },
        onBindingDied = { coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.BINDING_DIED) } },
        onNullBinding = { coroutineScope.launch { _binderLifecycleEvents.emit(BinderLifecycleEvent.NULL_BINDING) } }
    ).create()

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
