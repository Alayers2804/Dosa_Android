package com.skripsi.dosa

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _safeMessageItem = MutableLiveData<List<NotificationItemModel>>()
    val safeMessageItem: LiveData<List<NotificationItemModel>> = _safeMessageItem

    private val _safeMessagePerTagItem = MutableLiveData<List<NotificationItemModel>>()
    val safeMessagePerTagItem: LiveData<List<NotificationItemModel>> = _safeMessagePerTagItem

    private val _spamNotifications = MutableLiveData<List<NotificationItemModel>>()
    val spamNotifications: LiveData<List<NotificationItemModel>> = _spamNotifications


    private val notificationDao = AppDatabase.getDatabase(application).notificationDao()

    init {
        safeMessageItem
        fetchLatestNotificationsByTag()
//        fetchAllSafeNotifications()
        spamNotifications
    }


//    private fun fetchAllSafeNotifications() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val notifications = notificationDao.getNotificationItems()
//            } catch (e: Exception) {
//                Log.e("NotificationViewModel", "Error fetching safe notifications", e)
//            }
//        }
//    }

    fun addSafeNotification(notification: NotificationItemModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notificationDao.insert(notification)
                fetchLatestNotificationsByTag()
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error inserting safe notification", e)
            }
        }
    }

    fun updateSpamNotifications(notifications: NotificationItemModel) {
        val currentList = _spamNotifications.value.orEmpty().toMutableList()
        currentList.add(notifications)
        _spamNotifications.value = currentList
    }

    private fun fetchLatestNotificationsByTag() {
        viewModelScope.launch(Dispatchers.IO) { // Use IO dispatcher for database work
            try {
                val latestNotifications = notificationDao.getLatestNotificationPerTag()
                _safeMessageItem.postValue(latestNotifications)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error fetching latest notifications by tag", e)
            }
        }
    }

    fun fetchNotificationsByTag(tag: String) {
        viewModelScope.launch(Dispatchers.IO) { // Use IO dispatcher for database work
            try {
                val notifications = notificationDao.getNotificationsByTag(tag)
                _safeMessagePerTagItem.postValue(notifications)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error fetching notifications by tag", e)
            }
        }
    }
}


