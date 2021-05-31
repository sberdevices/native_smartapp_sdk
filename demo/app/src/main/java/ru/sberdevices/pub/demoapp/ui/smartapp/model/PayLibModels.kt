package ru.sberdevices.pub.demoapp.ui.smartapp.model

import kotlinx.serialization.Serializable

/**
 * Params to create a payment.
 * We need information about items to buy contained in [cardInfo]
 * Also we need about order contained in [orderInfo]
 */
@Serializable
data class BuyParameters(
    val cardInfo: CardInfo,
    val orderInfo: OrderInfo
)

/**
 * Applicable only to russian individual enterprise. Hence KD in russian.
 */
@Serializable
data class CardInfo(
    /**
     * Номер (идентификатор) товарной позиции в системе магазина. Параметр должен быть уникальным в рамках запроса
     */
    val position_id: Int,

    /**
     * Наименование или описание товарной позиции
     */
    val name: String,

    /**
     * Цена единицы товарной позиции. Указывается без разделителя, в копейках
     */
    val item_price: Int,

    /**
     * Количество и название сущности в которой считаются товары
     */
    val quantity: Quantity,

    /**
     * Общая цена всех единиц товарной позиции. Указывается без разделителя, в копейках
     */
    val item_amount: Int,

    /**
     * Код валюты в формате ISO 4217.
     */
    val currency: String = "RUB",

    /**
     * Номер (идентификатор) товарной позиции в системе магазина. Параметр должен быть уникальным в рамках запроса
     */
    val item_code: String,

    /**
     * Значение ставки НДС:
     *
     * 0: без НДС
     * 1: НДС по ставке 0%
     * 2: НДС по ставке 10%
     * 3: НДС по ставке 18%
     * 4: НДС по ставке 10/110
     * 5: НДС по ставке 18/118
     * 6: НДС по ставке 20%
     * 7: НДС по ставке 20/120
     *
     * Значение "НДС по ставке 0%" отличается от варианта "без НДС" только формированием чека
     * в зависимости от системы налогооблажения. По сумме налога разницы нет
     *
     */
    val tax_type: Int
)

/**
 * Quantity [value] of item and sense of what items are measured in [measure]
 */
@Serializable
data class Quantity(
    val value: Int,
    val measure: String
)

/**
 * Applicable only to russian individual enterprise. Hence KD in russian.
 */
@Serializable
data class OrderInfo(
    /**
     * Идентификатор заказа для сервиса платежей.
     * Должен быть уникален в рамках выделенного для приложения service_id
     */
    val order_id: String,

    /**
     * Номер заказа для пользователя в произвольном формате, необязательное поле
     */
    val order_number: String,

    /**
     * Сумма заказа, **должна совпадать** с суммой всех позиций
     */
    val amount: Int,

    /**
     * Наименование вашего юридического лица
     */
    val purpose: String,

    /**
     * Описание платежа для отображения пользователю
     */
    val description: String,

    /**
     * Из зявки на подключение платежей, его выдают вместе с ключом
     */
    val service_id: String,

    /**
     * Система налогообложения:
     * 0 – общая;
     * 1 – упрощенная, доход;
     * 2 – упрощенная, доход минус расход;
     * 3 – единый налог на вмененный доход;
     * 4 – единый сельскохозяйственный налог;
     * 5 – патентная система налогообложения
     */
    val tax_system: Int,

    /**
     * Код валюты в формате ISO-4217
     */
    val currency: String = "RUB",

    /**
     * Язык, на котором передаются все текстовые поля в запросе
     */
    val language: String = "ru-RU"
)