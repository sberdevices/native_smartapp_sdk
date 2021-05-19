# Native-App SDK для умных устройств SberDevices

**SmartApp** — это приложение для умных устройств SberDevices со своим фронтендом и бэкендом. 
Роль фронтенда выполняет Native App — это Android-приложение в формате apk.
Бекенд можно разработать в [SmartApp Studio](http://smartapp-studio.sberdevices.ru/).

В данном репозитории содержатся библиотеки SDK для умных устройств SberDevices.
Эти библотеки позволяют использовать возможности умных устройств, например, использование ассистента Салют или возможности визуального распознавания на устройствах.
 
# Библиотеки
* **AppState** — чтобы сценарий смартапа понимал, что происходит в клиентской части у пользователя, ему необходимо получать текущее состояние android-приложения. Это состояние можно передать через данную библиотеку. Состояние передается в формате JSON вместе с каждым голосовым запросом пользователя
* **Messaging** — обмен сообщениями между ассистентом и сценарием смартапа возможен через эту библиотеку. Они позволяет смартапу передавать в сценарий информацию о действиях пользователя, а сценарию — оповещать смартап о голосовых запросах
* **mic-camera-state** — служит для получения текущего состояния микрофона и камеры устройства
* **cv** и **camera** — чтобы распознавать позы и жесты с камер на встроенных устройствах. Библиотека работает на умных устройствах с камерой
* Вспомогательные библиотеки: **asserts**, **camera**, **logger** и **binderhelper**

# Использование

Для подключения нужен репозиторий *mavenCentral()*. Эти библиотеки подключаются как:
```Groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation "ru.sberdevices.smartapp.sdk:appstate:1.0-alpha"
        implementation "ru.sberdevices.smartapp.sdk:messaging:1.0-alpha"
        implementation "ru.sberdevices.smartapp.sdk:mic_camera_state:1.0-alpha"
        implementation "ru.sberdevices.smartapp.sdk:asserts:1.0"
        implementation "ru.sberdevices.smartapp.sdk:logger:1.0"
        implementation "ru.sberdevices.smartapp.sdk:camera:1.0-rc2"
        implementation "ru.sberdevices.smartapp.sdk:cv:3.0.0"
    }
}
```

# Документация
С документацией по разработке Native App для умных устройств SberDevices вы можете на [портале разработчика](https://developer.sberdevices.ru/docs/ru/methodology/research/nativeapp). 

# Поддержка
Для сообщений о проблемах и предложений по развитию библиотек пожалуйста используйте https://github.com/sberdevices/native_smartapp_sdk/issues
