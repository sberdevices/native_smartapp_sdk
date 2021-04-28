package ru.sberdevices.services.appstate;

import ru.sberdevices.services.appstate.IAppStateProvider;

interface IAppStateService {
   const int VERSION = 1;

   void setProvider(IAppStateProvider provider) = 110;
}
