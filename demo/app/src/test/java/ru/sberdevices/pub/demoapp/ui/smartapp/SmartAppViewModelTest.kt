package ru.sberdevices.pub.demoapp.ui.smartapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import ru.sberdevices.messaging.MessageId
import ru.sberdevices.messaging.Messaging
import ru.sberdevices.messaging.Payload
import ru.sberdevices.pub.demoapp.ui.smartapp.ui.SmartAppViewModel
import ru.sberdevices.services.appstate.AppStateHolder

// TODO fix test
/**
 * Test for [SmartAppViewModel]
 */
class SmartAppViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val messaging: Messaging = mockk()
    private val appStateHolder: AppStateHolder = mockk()

    @Test
    fun getClothes() {
        val addListener = slot<Messaging.Listener>()
        every { messaging.addListener(listener = capture(addListener)) } just Runs
        val myState = slot<String>()
        every { appStateHolder.setState(state = capture(myState)) } just Runs
        val smartAppViewModel = SmartAppViewModel(messaging, appStateHolder)

        addListener.captured.onMessage(MessageId("someId"), Payload(payloadWithBeanie))

        assertEquals(Clothes.BEANIE, smartAppViewModel.clothes.value)
        assertEquals(myState.captured, stateWithBeanie)
    }

    @Test
    fun getBuyItems() {
        val addListener = slot<Messaging.Listener>()
        every { messaging.addListener(listener = capture(addListener)) } just Runs
        val smartAppViewModel = SmartAppViewModel(messaging, appStateHolder)

        addListener.captured.onMessage(MessageId("someId"), Payload(payloadBuyElephant))

        assertEquals(BuyItems.ELEPHANT, smartAppViewModel.buyItems.value)
    }

    @Test
    fun onCleared() {
        val addListener = slot<Messaging.Listener>()
        val removeListener = slot<Messaging.Listener>()
        every { messaging.addListener(listener = capture(addListener)) } just Runs
        every { messaging.removeListener(listener = capture(removeListener)) } just Runs
        val smartAppViewModel = SmartAppViewModel(messaging, appStateHolder)

        smartAppViewModel.onCleared()

        verify { messaging.removeListener(any()) }
        assertTrue(addListener.captured == removeListener.captured)
    }

    companion object {
        const val payloadWithBeanie = "{ command: \"wear_this\", clothes: \"шапку\" }"
        const val payloadBuyElephant = "{ command: \"buy_success\", buyItems: [\"elephant\"] }"
        const val stateWithBeanie = "{\"myState\":\"На андроиде шапка\"}"
    }
}