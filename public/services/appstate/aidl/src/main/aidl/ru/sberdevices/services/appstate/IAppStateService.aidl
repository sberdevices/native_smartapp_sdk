package ru.sberdevices.services.appstate;

import ru.sberdevices.services.appstate.IAppStateProvider;
import ru.sberdevices.services.appstate.IAppStateStatusListener;

interface IAppStateService {
   const int VERSION = 2;

   void setProvider(@nullable IAppStateProvider provider) = 110;
   void setProviderForApp(@nullable IAppStateProvider provider, String packageName) = 111;

   void addAppStateStatusListener(in IAppStateStatusListener listener) = 120;
   void removeAppStateStatusListener(in IAppStateStatusListener listener) = 121;

   void registerBackgroundApp(String packageName) = 130;
   void unregisterBackgroundApp(String packageName) = 131;
}
