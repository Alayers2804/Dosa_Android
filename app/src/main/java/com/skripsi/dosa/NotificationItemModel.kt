package com.skripsi.dosa

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItemModel(val id:Long, val tag: String, @PrimaryKey val title: String, val text: String?, val postTime: String?, val packageName: String?, var spam: Boolean) :
    Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readBoolean()
    )

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
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): NotificationItemModel {
            return NotificationItemModel(parcel)
        }

        override fun newArray(size: Int): Array<NotificationItemModel?> {
            return arrayOfNulls(size)
        }
    }
}
