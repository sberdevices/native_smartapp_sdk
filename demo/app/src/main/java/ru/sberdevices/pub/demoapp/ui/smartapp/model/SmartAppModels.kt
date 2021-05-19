package ru.sberdevices.pub.demoapp.ui.smartapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Items variants
 */
@Serializable
enum class Clothes(val clothes: String) {
    @SerialName("шапку")
    BEANIE("шапка"),

    @SerialName("перчатки")
    GLOVES("перчатки"),

    @SerialName("ботинки")
    BOOTS("ботинки"),

    @SerialName("куртку")
    JACKET("куртка")
}

/**
 * Base command from smartapp backend
 */
@Serializable
sealed class BaseCommand

/**
 * Command for dressing up the Android
 */
@Serializable
@SerialName("wear_this")
internal class WearThisCommand(
    val clothes: Clothes? = null
): BaseCommand()

/**
 * Command for undressing the Android
 */
@Serializable
@SerialName("dont_wear_anything")
internal class ClearClothesCommand: BaseCommand()

/**
 * Command for successful purchase
 */
@Serializable
@SerialName("buy_success")
internal class BuySuccessCommand(
    val buyItems: List<BuyItems>? = null
): BaseCommand()

/**
 * Command for failed purchase
 */
@Serializable
@SerialName("buy_fail")
internal class BuyFailCommand(
): BaseCommand()

/**
 * Items for purchase
 */
@Serializable
enum class BuyItems {
    @SerialName("elephant")
    ELEPHANT
}

/**
 * JSON [myState] state that is pulled to smartapp backend
 */
@Serializable
internal data class MyAppState(
    val myState: String
)

/**
 * Send intent to do some action on smart app backend. It can be some event in game, or some input from user.
 * Event can be caught on server side by its [actionId] and carry some useful payload in it's [parameters]
 */
@Serializable
internal data class ServerAction(
    @SerialName("action_id")
    val actionId: String,

    @SerialName("parameters")
    val parameters: Map<String, String>
)