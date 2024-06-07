package com.skripsi.dosa

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {

    val safeMessageItem: MutableLiveData<MutableList<NotificationItemModel>> = MutableLiveData()
    val spamMessageItem: MutableLiveData<MutableList<NotificationItemModel>> = MutableLiveData()

    init {
        safeMessageItem.value = mutableListOf()
        spamMessageItem.value = mutableListOf()
    }

    fun addSafeNotification(notification: NotificationItemModel) {
        val updatedList = safeMessageItem.value ?: mutableListOf()
        updatedList.add(notification)
        safeMessageItem.value = updatedList
    }

    fun addSpamNotification(notification: NotificationItemModel){
        val updatedList = spamMessageItem.value ?: mutableListOf()
        updatedList.add(notification)
        spamMessageItem.value = updatedList
    }
}