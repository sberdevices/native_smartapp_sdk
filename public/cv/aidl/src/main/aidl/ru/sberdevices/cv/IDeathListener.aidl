package ru.sberdevices.cv;

interface IDeathListener {
    oneway void onDeath(in int bindingId) = 10;
}
