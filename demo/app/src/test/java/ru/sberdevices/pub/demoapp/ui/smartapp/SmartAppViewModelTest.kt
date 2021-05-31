package ru.sberdevices.pub.demoapp.ui.smartapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.sberdevices.messaging.MessageId
import ru.sberdevices.messaging.MessageName
import ru.sberdevices.messaging.Messaging
import ru.sberdevices.messaging.Payload
import ru.sberdevices.pub.demoapp.ui.smartapp.model.BuyParameters
import ru.sberdevices.pub.demoapp.ui.smartapp.model.CardInfo
import ru.sberdevices.pub.demoapp.ui.smartapp.model.OrderInfo
import ru.sberdevices.pub.demoapp.ui.smartapp.model.Quantity
import ru.sberdevices.pub.demoapp.ui.smartapp.ui.SmartAppViewModel
import ru.sberdevices.services.appstate.AppStateHolder

/**
 * Test for [SmartAppViewModel]
 */
@ExperimentalCoroutinesApi
class SmartAppViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private val messaging: Messaging = mockk()
    private val appStateHolder: AppStateHolder = mockk()

    @Test
    fun getClothes() = runBlocking {
        val addListener = slot<Messaging.Listener>()
        every { messaging.addListener(listener = capture(addListener)) } just Runs
        val myState = slot<String>()
        every { appStateHolder.setState(state = capture(myState)) } just Runs
        val smartAppViewModel = SmartAppViewModel(messaging, appStateHolder, testCoroutineDispatcher)

        addListener.captured.onMessage(MessageId("someId"), Payload(payloadWithBeanie))

        assertEquals(Clothes.BEANIE, smartAppViewModel.clothes.first())
        assertEquals(myState.captured, stateWithBeanie)
    }

    @Test
    fun getBuyItems() = runBlocking {
        val addListener = slot<Messaging.Listener>()
        every { messaging.addListener(listener = capture(addListener)) } just Runs
        every { appStateHolder.setState(any()) } just Runs
        val smartAppViewModel = SmartAppViewModel(messaging, appStateHolder, testCoroutineDispatcher)

        addListener.captured.onMessage(MessageId("someId"), Payload(payloadBuyElephant))

        assertEquals(BuyItems.ELEPHANT, smartAppViewModel.buyItems.first())
    }

    @Test
    fun addItemsToCartAndPay() {
        val addListener = slot<Messaging.Listener>()
        val messageNameListener = slot<MessageName>()
        val payloadListener = slot<Payload>()
        every { messaging.addListener(listener = capture(addListener)) } just Runs
        every {
            messaging.sendAction(messageName = capture(messageNameListener), payload = capture(payloadListener))
        } returns MessageId("1")
        val smartAppViewModel = SmartAppViewModel(messaging, appStateHolder, testCoroutineDispatcher)

        smartAppViewModel.addItemsToCartAndPay()

        assertTrue(messageNameListener.captured == MessageName.SERVER_ACTION)
        val decoded = Json.decodeFromString<ServerAction<BuyParameters>>(payloadListener.captured.data)
        assertEquals(testCardInfo, decoded.parameters.cardInfo)
        assertEquals(
                OrderInfo(
                order_id = decoded.parameters.orderInfo.order_id,
                order_number = "1",
                description = "Покупка слона",
                tax_system = 0,
                amount = testCardInfo.item_amount,
                purpose = "OOO Elephant Seller",
                service_id = "27"
            ),
            decoded.parameters.orderInfo, )
    }

    companion object {
        const val payloadWithBeanie = "{ \"command\": \"wear_this\", \"clothes\": \"шапку\" }"
        const val payloadBuyElephant = "{\"buyItems\":[\"elephant\"],\"command\":\"buy_success\",\"invoiceId\":\"635832\",\"orderBundle\":[{\"currency\":\"RUB\",\"item_amount\":100,\"item_code\":\"ru.some.elephant\",\"item_params\":[],\"item_price\":100,\"name\":\"New Elephant\",\"position_id\":1,\"quantity\":{\"measure\":\"thing\",\"value\":1},\"tax_type\":6}]}"
        const val stateWithBeanie = "{\"myState\":\"На андроиде шапка\"}"

        val testCardInfo = CardInfo(
            1,
            name = "New Elephant",
            item_price = 100,
            item_amount = 100,
            item_code = "ru.some.elephant",
            tax_type = 6,
            quantity = Quantity(
                1,
                "thing")
        )
    }
}
