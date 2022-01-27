package ru.sberdevices.services.appstate;

interface IAppStateStatusListener {
   oneway void onAppStateConnected() = 10;
}
