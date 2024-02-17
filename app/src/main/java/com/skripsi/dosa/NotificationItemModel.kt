package com.skripsi.dosa

import android.os.Parcel
import android.os.Parcelable

data class NotificationItemModel(val tag: String?, val title: String?, val text: String?, val postTime: String?, val packageName: String?) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tag)
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeString(postTime)
        parcel.writeString(packageName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotificationItemModel> {
        override fun createFromParcel(parcel: Parcel): NotificationItemModel {
            return NotificationItemModel(parcel)
        }

        override fun newArray(size: Int): Array<NotificationItemModel?> {
            return arrayOfNulls(size)
        }
    }
}
