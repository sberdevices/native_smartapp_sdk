package ru.sberdevices.services.appstate.description

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_DEEP_LINK
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_ID
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_IGNORED_WORDS
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_ITEMS
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_ITEM_SELECTOR
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_LOCAL_ACTION
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_NUMBER
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_SERVER_ACTION
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_TITLE
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_TYPE
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.KEY_VISIBLE
import ru.sberdevices.services.appstate.description.AppStateDescriber.Companion.VALUE_TYPE_DEEP_LINK
import ru.sberdevices.services.appstate.description.entity.Action
import ru.sberdevices.services.appstate.description.entity.AppState
import ru.sberdevices.services.appstate.description.entity.DeepLinkAction
import ru.sberdevices.services.appstate.description.entity.LocalAction
import ru.sberdevices.services.appstate.description.entity.ServerAction

internal class AppStateDescriberImpl(private val state: AppState) : AppStateDescriber {

    private val logger = Logger.get("AppStateDescriberImpl")

    override fun describe(): String {
        return try {
            val stateJson = JSONObject()
            val itemSelector = state.itemSelector
            if (itemSelector != null) {
                val itemSelectorJson = JSONObject()
                val ignoredWords = itemSelector.ignoredWords
                if (ignoredWords.isNotEmpty()) {
                    val ignoredWordsJson = JSONArray()
                    ignoredWords.forEach { word -> ignoredWordsJson.put(word) }
                    itemSelectorJson.put(KEY_IGNORED_WORDS, ignoredWordsJson)
                }
                val items = itemSelector.items
                if (items.isNotEmpty()) {
                    val itemsJson = JSONArray()
                    items.forEach { item ->
                        val itemJson = JSONObject().apply {
                            put(KEY_NUMBER, item.number)
                            put(KEY_ID, item.id)
                            put(KEY_TITLE, item.title)
                            put(KEY_VISIBLE, item.visible)
                            describeAction(this, item.action)
                        }
                        itemsJson.put(itemJson)
                    }
                    itemSelectorJson.put(KEY_ITEMS, itemsJson)
                }
                stateJson.put(KEY_ITEM_SELECTOR, itemSelectorJson)
            }
            stateJson.toString()
        } catch (e: JSONException) {
            logger.error(e) { "Cannot describe app state" }
            JSONObject().toString()
        }
    }

    private fun describeAction(itemJson: JSONObject, action: Action) {
        when (action) {
            is LocalAction -> itemJson.put(KEY_LOCAL_ACTION, describeLocalAction(action))
            is ServerAction -> itemJson.put(KEY_SERVER_ACTION, describeServerAction(action))
        }
    }

    private fun describeLocalAction(action: LocalAction): JSONObject {
        return when (action) {
            is DeepLinkAction -> describeDeepLinkAction(action)
            // For now only deeplink local action has known format, feel free to add required parsing for your action
            else -> JSONObject()
        }
    }

    private fun describeDeepLinkAction(action: DeepLinkAction): JSONObject {
        return JSONObject().apply {
            put(KEY_TYPE, VALUE_TYPE_DEEP_LINK)
            put(KEY_DEEP_LINK, action.deepLink)
        }
    }

    private fun describeServerAction(@Suppress("UNUSED_PARAMETER") action: ServerAction): JSONObject {
        // For now only local action has known format, feel free to add required parsing for your server action
        return JSONObject()
    }
}
