package com.skripsi.dosa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications")
    fun getNotificationItems(): List<NotificationItemModel>

    @Insert
    suspend fun insert(item: NotificationItemModel)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("SELECT * FROM notifications WHERE tag = :query")
    fun getNotificationsByTag(query: String): List<NotificationItemModel>

    @Query("SELECT * FROM notifications WHERE id IN (SELECT MAX(id) FROM notifications GROUP BY tag)")
    fun getLatestNotificationPerTag(): List<NotificationItemModel>
}

