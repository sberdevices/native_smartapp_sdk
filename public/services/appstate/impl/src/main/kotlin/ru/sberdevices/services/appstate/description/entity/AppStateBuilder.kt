package ru.sberdevices.services.appstate.description.entity

class AppStateBuilder {

    internal var itemSelector: ItemSelector? = null

    internal fun build(): AppState {
        return object: AppState {
            override val itemSelector: ItemSelector? = this@AppStateBuilder.itemSelector
        }
    }
}
