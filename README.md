# Native-App SDK для умных устройств SberDevices

SmartApp — это приложение для умных устройств SberDevices со своим фронтендом и бэкендом. 
Роль фронтенда выполняет Native App — это Android-приложение в формате apk.

В данном репозитории содержатся библиотеки SDK для умных устройств SberDevices.
Эти библотеки позволяют использовать возможности умных устройств, например использование ассистента Салют или возможности визуального распознавания на устройствах.
 
# Библиотеки
* AppState -- Чтобы сценарий смартапа понимал, что происходит в клиентской части у пользователя, ему необходимо получать текущее состояние android-приложения. Это состояние можно передать через данную библиотеку. Состояние передается в формате json с каждым голосовым запросом пользователя.
* Messaging -- Обмен сообщениями между ассистентом и сценарием смартапа возможен через эту библиотеку. Они позволяет смартапу передавать в сценарий информацию о действиях пользователя, а сценарию — оповещать смартап о голосовых запросах.
* mic-camera-state -- служит для получения текущего состояния микрофона и камеры устройства 
* cv-api, чтобы распознавать позы и жесты с камер на встроенных устройствах. Библиотека работает на устройствах SberPortal и SberBox Top. 
* Вспомогательные либы - TODO!!!

Эти библиотеки подключаются как 

для подключения нужен репозиторий mavenCentral()

TODO поправить названия и версии после выкладки 

>buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        implementation "ru.sberdevices.smartapp.sdk:asserts:0.1"
        implementation "ru.sberdevices.smartapp.sdk:logger:0.1"
        implementation "ru.sberdevices.smartapp.sdk:binderhelper:0.1"
        implementation "ru.sberdevices.smartapp.sdk:binderhelper_api:0.1"
        implementation "ru.sberdevices.smartapp.sdk.appstate:api:0.1"
        implementation "ru.sberdevices.smartapp.sdk.appstate:impl:0.1"
    }
}

Гайд по библотекам https://sbtatlas.sigma.sbrf.ru/wiki/display/SMDG/%5BSDTW-56%5D+Native+App TODO

# Скриншоты
TODO

# Поддержка
Для сообщений о проблемах и предложений по развитию библиотек пожалуйста используйте https://github.com/sberdevices/native_smartapp_sdk/issues