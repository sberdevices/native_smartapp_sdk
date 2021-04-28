package ru.sberdevices.services.appstate.description.entity

interface ItemSelector {

    val ignoredWords: List<String>

    val items: List<Item>
}
