package ru.sberdevices.cv.detection.entity;

interface IHumansDetectionListener {
    oneway void onUpdate(in byte[] humans) = 10;
}