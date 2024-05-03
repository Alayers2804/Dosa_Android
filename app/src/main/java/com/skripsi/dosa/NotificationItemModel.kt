package com.skripsi.dosa

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotificationItemModel(@PrimaryKey(autoGenerate = true) val id: Long = 0, val tag: String?, val title: String?, val text: String?, val postTime: String?, val packageName: String?) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
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
