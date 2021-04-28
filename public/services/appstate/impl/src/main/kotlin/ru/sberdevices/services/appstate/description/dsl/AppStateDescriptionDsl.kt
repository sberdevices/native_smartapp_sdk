package ru.sberdevices.services.appstate.description.dsl

import ru.sberdevices.services.appstate.description.AppStateDescriber
import ru.sberdevices.services.appstate.description.AppStateDescriberImpl
import ru.sberdevices.services.appstate.description.entity.Action
import ru.sberdevices.services.appstate.description.entity.AppStateBuilder
import ru.sberdevices.services.appstate.description.entity.DeepLinkAction
import ru.sberdevices.services.appstate.description.entity.ItemBuilder
import ru.sberdevices.services.appstate.description.entity.ItemSelectorBuilder

fun appState(description: AppStateBuilder.() -> Unit): AppStateDescriber {
    return AppStateDescriberImpl(AppStateBuilder().apply(description).build())
}

fun AppStateBuilder.selectableItems(selectableItemsDescription: ItemSelectorBuilder.() -> Unit) {
    check(itemSelector == null) { "Item selector already initialized earlier" }
    itemSelector = ItemSelectorBuilder().apply(selectableItemsDescription).build()
}

fun ItemSelectorBuilder.addItem(itemDescription: ItemBuilder.() -> Unit) {
    addItem(ItemBuilder().apply(itemDescription).build())
}

fun deepLinkAction(deepLink: String): Action = DeepLinkAction(deepLink)
