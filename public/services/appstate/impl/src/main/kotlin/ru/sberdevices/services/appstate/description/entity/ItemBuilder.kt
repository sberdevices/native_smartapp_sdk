package ru.sberdevices.services.appstate.description.entity

class ItemBuilder {

    var index: Int? = null
    var id: String? = null
    var visible: Boolean = true
    var title: String? = null
    var action: Action? = null

    internal fun build(): Item {
        return object : Item {
            override val index: Int = requireNotNull(this@ItemBuilder.index)
            override val id: String = requireNotNull(this@ItemBuilder.id)
            override val visible: Boolean = this@ItemBuilder.visible
            override val title: String = requireNotNull(this@ItemBuilder.title)
            override val action: Action = requireNotNull(this@ItemBuilder.action)
        }
    }
}
