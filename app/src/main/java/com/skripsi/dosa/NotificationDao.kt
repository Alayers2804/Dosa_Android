package com.skripsi.dosa

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications")
    fun getNotificationItems(): List<NotificationItemModel>

    @Insert
    suspend fun insert(item: NotificationItemModel)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
