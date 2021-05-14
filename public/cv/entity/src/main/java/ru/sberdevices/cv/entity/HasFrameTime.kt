package ru.sberdevices.cv.entity

import android.os.SystemClock

interface HasFrameTime {
    val frameTimestampMs: Long

    fun getTimePassedAfterFrameMs(): Long {
        return SystemClock.elapsedRealtime() - frameTimestampMs
    }
}
