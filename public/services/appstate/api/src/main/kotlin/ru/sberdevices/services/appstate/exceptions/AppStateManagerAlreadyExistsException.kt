package ru.sberdevices.services.appstate.exceptions

class AppStateManagerAlreadyExistsException : RuntimeException("Must be only one active instance of AppStateManager")
