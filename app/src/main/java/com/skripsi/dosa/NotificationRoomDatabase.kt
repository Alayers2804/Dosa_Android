package com.skripsi.dosa

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [NotificationItemModel::class], version = 1, exportSchema = false)
abstract class NotificationRoomDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    private class NotificationDatabseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                scope.launch {

                }
            }
        }
        suspend fun populateDatabase(notificationDao: NotificationDao) {
            // Delete all content here.
            notificationDao.deleteAll()

            // Add sample words.
            val notificationItem = NotificationItemModel(1,"akjfkalda;lpo","DB testing", "Hai ini aku Ferdinand", "28-04-2024", "ferdinand")
            notificationDao.insert(notificationItem)
            // TODO: Add your own words!
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NotificationRoomDatabase? = null

        fun getDatabase(context: Context,scope: CoroutineScope): NotificationRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationRoomDatabase::class.java,
                    "notification_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}