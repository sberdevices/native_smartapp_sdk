package ru.sberdevices.services.appstate.description.entity

interface Item {

    val number: Int

    val id: String

    val visible: Boolean

    val title: String

    val action: Action
}
