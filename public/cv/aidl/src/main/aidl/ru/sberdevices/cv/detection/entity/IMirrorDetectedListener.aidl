package ru.sberdevices.cv.detection.entity;

interface IMirrorDetectedListener {
    oneway void onUpdate(in boolean detected) = 10;
}