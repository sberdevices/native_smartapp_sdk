package ru.sberdevices.pub.demoapp.ui.smartapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.messaging.MessageId
import ru.sberdevices.messaging.MessageName
import ru.sberdevices.messaging.Messaging
import ru.sberdevices.messaging.Payload
import ru.sberdevices.pub.demoapp.ui.smartapp.BaseCommand
import ru.sberdevices.pub.demoapp.ui.smartapp.BuyItems
import ru.sberdevices.pub.demoapp.ui.smartapp.BuySuccessCommand
import ru.sberdevices.pub.demoapp.ui.smartapp.ClearClothesCommand
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes
import ru.sberdevices.pub.demoapp.ui.smartapp.MyAppState
import ru.sberdevices.pub.demoapp.ui.smartapp.ServerAction
import ru.sberdevices.pub.demoapp.ui.smartapp.WearThisCommand
import ru.sberdevices.pub.demoapp.ui.smartapp.model.BuyParameters
import ru.sberdevices.pub.demoapp.ui.smartapp.model.CardInfo
import ru.sberdevices.pub.demoapp.ui.smartapp.model.OrderInfo
import ru.sberdevices.pub.demoapp.ui.smartapp.model.Quantity
import ru.sberdevices.services.appstate.AppStateHolder
import java.util.UUID.randomUUID

/**
 * In this example view model gets messages from smartapp backend by [Messaging].
 * Also it shares its state with smartapp backend via [AppStateHolder].
 */
@ExperimentalCoroutinesApi
class SmartAppViewModel(
    private val messaging: Messaging,
    private val appStateHolder: AppStateHolder,
    private val ioCoroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val json = Json { encodeDefaults = true }
    private val commandParser = Json {
        classDiscriminator = "command"
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val logger by Logger.lazy("SmartAppViewModel")
    private val currentClothes: MutableSet<Clothes> = HashSet()

    private val _clothes = MutableSharedFlow<Clothes?>(
        replay = Clothes.values().size
    )
    private val _buyItems = MutableSharedFlow<BuyItems>(
        replay = 1
    )

    val buyItems: SharedFlow<BuyItems> = _buyItems.asSharedFlow()
    val clothes = _clothes.asSharedFlow()

    @ExperimentalCoroutinesApi
    private val listener = object : Messaging.Listener {
        override fun onMessage(messageId: MessageId, payload: Payload) {
            logger.debug { "Message ${messageId.value} received: ${payload.data}" }

            val model = commandParser.decodeFromString<BaseCommand>(payload.data)

            when (model) {
                is WearThisCommand -> {
                    model.clothes?.let {
                        currentClothes.add(it)
                        _clothes.tryEmit(model.clothes)
                    }
                }
                is ClearClothesCommand -> {
                    currentClothes.clear()
                    _clothes.resetReplayCache()
                    _clothes.tryEmit(null)
                }
                is BuySuccessCommand -> {
                    if (model.orderBundle.firstOrNull { it.item_code == PAYLIB_ITEM_CODE} != null ) {
                        _buyItems.tryEmit(BuyItems.ELEPHANT)
                    }
                }
            }

            // send current state to smartapp backend
            appStateHolder.setState(
                Json.encodeToString(
                    MyAppState("На андроиде ${currentClothes.joinToString(transform = { it.clothes })}")
                )
            )
        }

        override fun onError(messageId: MessageId, throwable: Throwable) {
            logger.error { throwable.stackTraceToString() }
        }
    }

    init {
        messaging.addListener(listener)
    }

    fun addItemsToCartAndPay() {
        val cardInfo = CardInfo(
            1,
            name = "New Elephant",
            item_price = 100,
            item_amount = 100, // must be item_price*quantity.value,
            item_code = PAYLIB_ITEM_CODE,
            tax_type = 6,
            quantity = Quantity(
                1,
                "thing")
        )
        val orderInfo = OrderInfo(
            order_id = randomUUID().toString(),
            order_number = "1",
            description = "Покупка слона",
            tax_system = 0,
            amount = cardInfo.item_amount,
            purpose = PAYLIB_ORGANISATION,
            service_id = PAYLIB_SERVICE_ID
        )

        viewModelScope.launch(ioCoroutineDispatcher) {
            messaging.sendAction(
                MessageName.SERVER_ACTION,
                formBuyServerActionPayload(
                    cardInfo = cardInfo,
                    orderInfo = orderInfo
                )
            )
        }
    }

    private fun formBuyServerActionPayload(cardInfo: CardInfo, orderInfo: OrderInfo): Payload =
        Payload(
            json.encodeToString(
                ServerAction(
                    actionId = "ACTION_FROM_NATIVE_APP",
                    parameters = BuyParameters(
                        cardInfo = cardInfo,
                        orderInfo = orderInfo
                    )
                )
            )
        )

    private companion object {
        const val PAYLIB_ITEM_CODE = "ru.some.elephant"
        const val PAYLIB_ORGANISATION = "OOO Elephant Seller"
        const val PAYLIB_SERVICE_ID = "27"
    }
}


