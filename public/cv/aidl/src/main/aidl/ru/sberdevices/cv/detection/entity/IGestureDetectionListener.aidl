package ru.sberdevices.cv.detection.entity;

interface IGestureDetectionListener {
    oneway void onUpdate(in byte[] gesture) = 10;
}