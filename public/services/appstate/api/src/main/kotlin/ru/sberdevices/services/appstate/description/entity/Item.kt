package ru.sberdevices.services.appstate.description.entity

interface Item {

    val index: Int

    val id: String

    val visible: Boolean

    val title: String

    val action: Action
}
