package ru.sberdevices.services.appstate.description

interface AppStateDescriber {

    fun describe(): String

    companion object {
        const val KEY_ITEM_SELECTOR = "item_selector"
        const val KEY_IGNORED_WORDS = "ignored_words"
        const val KEY_ITEMS = "items"
        const val KEY_NUMBER = "number"
        const val KEY_ID = "id"
        const val KEY_VISIBLE = "visible"
        const val KEY_TITLE = "title"
        const val KEY_LOCAL_ACTION = "action"
        const val KEY_SERVER_ACTION = "server_action"
        const val KEY_TYPE = "type"
        const val KEY_DEEP_LINK = "deep_link"

        const val VALUE_TYPE_DEEP_LINK = "deep_link"
    }
}
