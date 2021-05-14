package ru.sberdevices.cv

import android.os.Parcel
import android.os.Parcelable
import ru.sberdevices.cv.util.read
import ru.sberdevices.cv.util.write

data class ServiceInfo(
    val cvApiVersion: String?,
    val objectTrackingVersion: String?,
    val visionLabsVersion: String?,
    val visionLabsVersionHash: String?,
    val objectTrackingMetadata: String?
) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.write(convertToBytes())
    }

    private fun convertToBytes(): ByteArray {
        return ru.sberdevices.cv.proto.ServiceInfo.newBuilder()
            .apply {
                if (!this@ServiceInfo.cvApiVersion.isNullOrBlank()) {
                    cvApiVersion = this@ServiceInfo.cvApiVersion
                }
                if (!this@ServiceInfo.objectTrackingVersion.isNullOrBlank()) {
                    objectTrackingVersion = this@ServiceInfo.objectTrackingVersion
                }
                if (!this@ServiceInfo.visionLabsVersion.isNullOrBlank()) {
                    visionLabsVersion = this@ServiceInfo.visionLabsVersion
                }
                if (!this@ServiceInfo.visionLabsVersionHash.isNullOrBlank()) {
                    visionLabsVersionHash = this@ServiceInfo.visionLabsVersionHash
                }
                if (!this@ServiceInfo.objectTrackingMetadata.isNullOrBlank()) {
                    objectTrackingMetadata = this@ServiceInfo.objectTrackingMetadata
                }
            }
            .build()
            .toByteArray()
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServiceInfo> {
        override fun createFromParcel(parcel: Parcel): ServiceInfo {
            return parcel.read().convertToServiceInfo()
        }

        private fun ByteArray.convertToServiceInfo(): ServiceInfo {
            return ru.sberdevices.cv.proto.ServiceInfo.newBuilder()
                .mergeFrom(this)
                .build()
                .convertFromProto()
        }

        private fun ru.sberdevices.cv.proto.ServiceInfo.convertFromProto(): ServiceInfo {
            return ServiceInfo(
                cvApiVersion = cvApiVersion.takeUnless { it.isNullOrBlank() },
                objectTrackingVersion = objectTrackingVersion.takeUnless { it.isNullOrBlank() },
                visionLabsVersion = visionLabsVersion.takeUnless { it.isNullOrBlank() },
                visionLabsVersionHash = visionLabsVersionHash.takeUnless { it.isNullOrBlank() },
                objectTrackingMetadata = objectTrackingMetadata.takeUnless { it.isNullOrBlank() }
            )
        }

        override fun newArray(size: Int): Array<ServiceInfo?> {
            return arrayOfNulls(size)
        }
    }
}
