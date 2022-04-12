package ru.sberdevices.services.paylib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import ru.sberdevices.common.binderhelper.CachedBinderHelper
import ru.sberdevices.common.binderhelper.entities.BinderState
import ru.sberdevices.common.binderhelper.repeatOnState
import ru.sberdevices.common.coroutines.CoroutineDispatchers
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.paylib.aidl.wrappers.PayStatusListenerWrapper
import ru.sberdevices.services.paylib.entities.PayStatus

internal class PayLibImpl(
    private val helper: CachedBinderHelper<IPayLibService>,
    private val dispatchers: CoroutineDispatchers,
    private val payStatusListenerWrapper: PayStatusListenerWrapper,
    callbackScope: CoroutineScope,
) : PayLib {

    private val logger = Logger.get("PayLibImpl")

    private val payStatusFlow: SharedFlow<PayStatus>

    init {
        logger.debug { "init" }

        payStatusFlow = callbackFlow {
            logger.debug { "onStart()" }
            helper.connect()

            repeatOnState(helper, BinderState.CONNECTED) {
                logger.debug { "helper connected event received" }
                helper.execute { it.addPayStatusListener(payStatusListenerWrapper) }
            }

            payStatusListenerWrapper.payStatusFlow
                .onEach {
                    logger.debug { "received new payStatus: $it" }
                    trySend(it)
                }
                .flowOn(dispatchers.io)
                .launchIn(this)

            awaitClose {
                logger.debug { "awaitClose()" }
                // tryExecute, потому что scope может быть отменен из-за ошибки
                helper.tryExecuteWithResult { it.removePayStatusListener(payStatusListenerWrapper) }
                    .onSuccess { logger.debug { "Successfully removed listener" } }
                    .onFailure { logger.warn(it) { "Couldn't remove listener" } }

                helper.disconnect()
            }
        }.shareIn(
            callbackScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 3_000,
                replayExpirationMillis = 0
            ),
            replay = 0,
        )
    }

    override suspend fun launchPayDialog(invoiceId: String): Result<PayStatus> {
        logger.debug { "launchPayDialog, invoiceId: $invoiceId" }
        val launched = helper.executeWithResult { it.launchPayDialog(invoiceId) }
        return if (launched.isSuccess) {
            try {
                val first = payStatusFlow
                    .filter { it.invoiceId == invoiceId }
                    .first()
                Result.success(first)
            } catch (ex: NoSuchElementException) {
                Result.failure(ex)
            }
        } else {
            val exception = launched.exceptionOrNull()
            if (exception != null) {
                Result.failure(exception)
            } else {
                Result.failure(IllegalStateException("Unknown examples"))
            }
        }.also { result ->
            logger.debug { "launchPayDialog, invoiceId: $invoiceId result: $result" }
        }
    }
}
