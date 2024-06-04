package com.skripsi.dosa

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

class NotificationViewModel @SuppressLint("StaticFieldLeak") constructor(override val lifecycle: Lifecycle) : ViewModel(), LifecycleOwner {

    val safeMessageItem: MutableLiveData<MutableList<NotificationItemModel>> = MutableLiveData()
    val spamMessageItem: MutableLiveData<MutableList<NotificationItemModel>> = MutableLiveData()

    private val _clearData = MutableLiveData<Unit>()

    init {
        safeMessageItem.value = mutableListOf()
        spamMessageItem.value = mutableListOf()

        _clearData.observe(this, Observer { clearData() })
    }

    fun addNotification(notification: NotificationItemModel) {
        val updatedList = safeMessageItem.value ?: mutableListOf()
        updatedList.add(notification)
        safeMessageItem.value = updatedList
    }

    private fun clearData() {
        safeMessageItem.value = mutableListOf()
        spamMessageItem.value = mutableListOf()
    }

    override fun onCleared() {
        super.onCleared()
        _clearData.value = Unit
    }
}
