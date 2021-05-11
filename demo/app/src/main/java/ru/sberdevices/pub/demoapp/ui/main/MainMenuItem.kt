package ru.sberdevices.pub.demoapp.ui.main

import ru.sberdevices.services.pub.demoapp.R

enum class MainMenuItem(val text: String, val actionId: Int) {
    SMARTAPP("SmartApp", R.id.action_mainFragment_to_smartappFragment),
    ASSISTANT("Assistant", R.id.action_mainFragment_to_assistantFragment),
}