# SberDevices Native-App SDK of smart devices

**SmartApp** — is an app that can be launched on smart devices manufactured by SberDevices. SmartApp consists of front-end and back-end part. Front-end part is represented by Android App running on a device, using native libraries we provide.
Back-end part can be developed using the [SmartMarket Studio](https://developers.sber.ru/studio/) tools.
This repository contains SDK that enables use of Salut virtual assistants in your app, enriching UX for your users.
You can use Computer Vision libraries as well to get gestures and visual information recognition for you app.

# Libraries
* **AppState** is a small library that transmits current state of the app from back-end part of smartapp to the front-end. Data is transmitted via JSON message. The app state is *pulled* from native APK and transmitted to back-end with every user’s voice request.
* **Messaging** is a library that serves as messaging interface between native app and smartapp back-end. The library enables sending events (eg user’s actions) to smartapp back-end. It also allows native app receive messages from back-end (eg user’s voice requests).
* **mic-camera-state** is a library that provides current state of device camera and microphone (microphone can be disabled and camera can be covered).
* **cv** и **camera** libraries helps recognize user’s gestures and pose using devices camera. Works only on devices with built-in cameras.
* Additional libraries: **asserts**, **camera**, **logger** and **binderhelper**.

# Documentation
Documentation regarding development of Native Apps is available at [developer’s portal](https://developer.sberdevices.ru/docs/ru/methodology/research/nativeapp) (in Russian).

# Support
Please send your feedback and feature requests to our [issues board](https://github.com/sberdevices/native_smartapp_sdk/issues).
