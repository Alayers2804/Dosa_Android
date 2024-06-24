package com.skripsi.dosa

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.compose.ui.window.application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.log

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    val safeMessageItem: MutableLiveData<List<NotificationItemModel>> = MutableLiveData()
    private val _spamNotifications = MutableLiveData<List<NotificationItemModel>>()
    val spamNotifications: LiveData<List<NotificationItemModel>> = _spamNotifications


    private val notificationDao = AppDatabase.getDatabase(application).notificationDao()

    init {
        fetchAllSafeNotifications() // Fetch all safe notifications initially
    }

    fun fetchAllSafeNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notifications = notificationDao.getNotificationItems()
                safeMessageItem.postValue(notifications)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error fetching safe notifications", e)
            }
        }
    }

    fun addSafeNotification(notification: NotificationItemModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notificationDao.insert(notification)
                fetchAllSafeNotifications() // Refresh the list after inserting a new notification
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
}


