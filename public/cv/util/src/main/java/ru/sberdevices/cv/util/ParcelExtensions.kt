package ru.sberdevices.cv.util

import android.os.Parcel

fun Parcel.read(): ByteArray {
    return ByteArray(size = readInt()).apply { readByteArray(this) }
}

fun Parcel.write(bytes: ByteArray) {
    writeInt(bytes.size)
    writeByteArray(bytes)
}
