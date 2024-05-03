package com.skripsi.dosa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotificationDao {

    @Query("SELECT * FROM NotificationItemModel")
    fun getNotificationItems(): List<NotificationItemModel>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(items : NotificationItemModel)

    @Query("DELETE FROM notificationitemmodel")
    suspend fun deleteAll()

}