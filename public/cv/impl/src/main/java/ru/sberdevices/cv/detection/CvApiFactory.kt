package ru.sberdevices.cv.detection

import android.content.Context
import ru.sberdevices.cv.util.BindingIdStorage
import ru.sberdevices.cv.util.BindingIdStorageImpl

interface CvApiFactory {
    fun get(): CvApi
}

class CvApiFactoryImpl(
    private val context: Context,
    private val bindingIdStorage: BindingIdStorage = BindingIdStorageImpl()
) : CvApiFactory {
    override fun get(): CvApi {
        return CvDetectionApiBinding(context, bindingIdStorage)
    }
}
