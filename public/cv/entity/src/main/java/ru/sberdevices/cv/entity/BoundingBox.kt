package ru.sberdevices.cv.entity

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable

data class BoundingBox(
    val relativeLeft: Float,
    val relativeTop: Float,
    val relativeRight: Float,
    val relativeBottom: Float
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(relativeLeft)
        parcel.writeFloat(relativeTop)
        parcel.writeFloat(relativeRight)
        parcel.writeFloat(relativeBottom)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getRelativeWidth(): Float {
        return relativeRight - relativeLeft
    }

    fun getRelativeHeight(): Float {
        return relativeBottom - relativeTop
    }

    fun getAbsoluteLeft(widthPx: Int): Int {
        return (relativeLeft * widthPx).toInt()
    }

    fun getAbsoluteRight(widthPx: Int): Int {
        return (relativeRight * widthPx).toInt()
    }

    fun getAbsoluteTop(heightPx: Int): Int {
        return (relativeTop * heightPx).toInt()
    }

    fun getAbsoluteBottom(heightPx: Int): Int {
        return (relativeBottom * heightPx).toInt()
    }

    fun getAbsoluteWidth(widthPx: Int): Int {
        return (getRelativeWidth() * widthPx).toInt()
    }

    fun getAbsoluteHeight(heightPx: Int): Int {
        return (getRelativeHeight() * heightPx).toInt()
    }

    fun asAbsoluteRect(widthPx: Int, heightPx: Int): Rect {
        return Rect(
            getAbsoluteLeft(widthPx),
            getAbsoluteTop(heightPx),
            getAbsoluteRight(widthPx),
            getAbsoluteBottom(heightPx)
        )
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<BoundingBox> {
            override fun createFromParcel(parcel: Parcel): BoundingBox {
                return BoundingBox(parcel)
            }

            override fun newArray(size: Int): Array<BoundingBox?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        val FULL = BoundingBox(0f, 0f, 1f, 1f)

        @JvmStatic
        fun fromAbsolute(
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            fullWidth: Int,
            fullHeight: Int
        ): BoundingBox {
            require(fullWidth > 0 && fullHeight > 0) { "Invalid size: ${fullWidth}x$fullHeight" }
            val validHorizontalRange = 0..fullWidth
            val validVerticalRange = 0..fullHeight
            val validCoordinates = left in validHorizontalRange &&
                top in validVerticalRange &&
                right in validHorizontalRange &&
                bottom in validVerticalRange
            require(validCoordinates) {
                "Invalid coordinates: $left, $top, $right, $bottom"
            }
            return BoundingBox(
                relativeLeft = left.toFloat() / fullWidth.toFloat(),
                relativeTop = top.toFloat() / fullHeight.toFloat(),
                relativeRight = right.toFloat() / fullWidth.toFloat(),
                relativeBottom = bottom.toFloat() / fullHeight.toFloat()
            )
        }

        @JvmStatic
        fun fromRelative(
            left: Double,
            top: Double,
            right: Double,
            bottom: Double
        ): BoundingBox {
            return BoundingBox(
                relativeLeft = left.toFloat(),
                relativeTop = top.toFloat(),
                relativeRight = right.toFloat(),
                relativeBottom = bottom.toFloat()
            )
        }

        @JvmStatic
        fun fromRelative(
            left: Float,
            top: Float,
            right: Float,
            bottom: Float
        ): BoundingBox {
            return BoundingBox(
                relativeLeft = left,
                relativeTop = top,
                relativeRight = right,
                relativeBottom = bottom
            )
        }
    }
}
