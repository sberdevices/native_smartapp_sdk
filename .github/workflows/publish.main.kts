#!/usr/bin/env kotlin

val argLib = args.firstOrNull()

val publishCommand = when (argLib?.lowercase()) {
    "asserts" -> "public:common:asserts:publish"
    "binderhelper" -> "public:common:binderhelper:api:publish public:common:binderhelper:impl:publish"
    "camera" -> "public:common:camera:publish"
    "coroutines" -> "public:common:coroutines:publish"
    "logger" -> "public:common:logger:publish"
    "cv" -> "public:cv:aidl:publish public:cv:api:publish public:cv:entity:publish public:cv:impl:publish public:cv:proto:publish public:cv:util:publish"
    "appstate" -> "public:services:appstate:aidl:publish public:services:appstate:api:publish public:services:appstate:impl:publish"
    "messaging" -> "public:services:messaging:aidl:publish public:services:messaging:api:publish public:services:messaging:impl:publish"
    "mic_camera_state" -> "public:services:mic_camera_state:aidl:publish public:services:mic_camera_state:api:publish public:services:mic_camera_state:impl:publish"
    else -> "publish"
}

val gradleCommand = "./gradlew $publishCommand"

println(gradleCommand)