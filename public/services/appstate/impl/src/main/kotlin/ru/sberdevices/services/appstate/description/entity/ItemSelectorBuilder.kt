package ru.sberdevices.services.appstate.description.entity

class ItemSelectorBuilder {

    private val ignoredWords = mutableSetOf<String>()
    private val items = mutableListOf<Item>()

    internal fun addIgnoredWord(ignoredWord: String) = run { ignoredWords += ignoredWord }
    internal fun addItem(item: Item) = run { items += item }

    internal fun build(): ItemSelector {
        return object : ItemSelector {
            override val ignoredWords: List<String> = this@ItemSelectorBuilder.ignoredWords.toList()
            override val items: List<Item> = this@ItemSelectorBuilder.items
        }
    }
}
