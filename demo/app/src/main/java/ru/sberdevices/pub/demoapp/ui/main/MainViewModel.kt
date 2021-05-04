package ru.sberdevices.pub.demoapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections

class MainViewModel : ViewModel() {

    private val _navigation = MutableLiveData<NavDirections?>()

    val menuItem: LiveData<List<MainMenuItem>> = liveData {
        emit(
            MainMenuItem.values().asList()
        )
    }

    val navigation: LiveData<NavDirections?> = _navigation

    fun onItemClick(item: MainMenuItem) {
        _navigation.value = ActionOnlyNavDirections(item.actionId)
        _navigation.value = null
    }
}