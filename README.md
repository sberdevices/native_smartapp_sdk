# Native-App SDK для умных устройств SberDevices

**SmartApp** — это приложение для умных устройств SberDevices со своим фронтендом и бэкендом. 
Роль фронтенда выполняет Native App — это Android-приложение в формате apk.
Бекенд можно разработать в [SmartApp Studio](http://smartapp-studio.sberdevices.ru/).

В данном репозитории содержатся библиотеки SDK для умных устройств SberDevices.
Эти библотеки позволяют использовать возможности умных устройств, например использование ассистента Салют или возможности визуального распознавания на устройствах.
 
# Библиотеки
* **AppState** -- Чтобы сценарий смартапа понимал, что происходит в клиентской части у пользователя, ему необходимо получать текущее состояние android-приложения. Это состояние можно передать через данную библиотеку. Состояние передается в формате JSON вместе с каждым голосовым запросом пользователя,
* **Messaging** -- Обмен сообщениями между ассистентом и сценарием смартапа возможен через эту библиотеку. Они позволяет смартапу передавать в сценарий информацию о действиях пользователя, а сценарию — оповещать смартап о голосовых запросах,
* **mic-camera-state** -- служит для получения текущего состояния микрофона и камеры устройства,
* **cv-api**, чтобы распознавать позы и жесты с камер на встроенных устройствах. Библиотека работает на умных устройствах с камерой,
* Вспомогательные библиотеки: **logger** и **asserts**

TODO поправить названия и версии после выкладки 
Lля подключения нужен репозиторий *mavenCentral()*. Эти библиотеки подключаются как:
```Groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation "ru.sberdevices.smartapp.sdk:asserts:1.0-rc1"
        implementation "ru.sberdevices.smartapp.sdk:logger:1.0-rc1"
        implementation "ru.sberdevices.smartapp.sdk.appstate:impl:1.0-rc1"
        implementation "ru.sberdevices.smartapp.sdk.messaging:impl:1.0-rc1"
        implementation "ru.sberdevices.smartapp.sdk.mic_camera_state:impl:1.0-rc1"

        implementation "ru.sberdevices.smartapp.sdk:camera:1.0-rc1"
        implementation "ru.sberdevices.smartapp.sdk.cv:impl:3.0.0"
        implementation "ru.sberdevices.smartapp.sdk.cv:util:3.0.0"
    }
}
```

Гайд по библотекам https://sbtatlas.sigma.sbrf.ru/wiki/display/SMDG/%5BSDTW-56%5D+Native+App TODO

# Поддержка
Для сообщений о проблемах и предложений по развитию библиотек пожалуйста используйте https://github.com/sberdevices/native_smartapp_sdk/issues