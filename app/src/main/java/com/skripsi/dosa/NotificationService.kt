package com.skripsi.dosa

import android.annotation.SuppressLint
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import java.text.SimpleDateFormat
import java.util.Date

class NotificationService: NotificationListenerService() {
    
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SimpleDateFormat")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val extras = sbn?.notification?.extras

        val tag = sbn?.tag.toString()
        val title = extras?.getString("android.title") ?:"test"
        val text = extras?.getCharSequence("android.text").toString()
        val postedTime = sbn?.postTime ?: 0
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(postedTime))
        val packageName = sbn?.packageName

        val notificationData = NotificationItemModel(tag, title, text, date.toString(), packageName)

        Log.d("MessageNotification", notificationData.toString())

        if(sbn!!.packageName == "com.whatsapp.w4b" || sbn.packageName == "com.whatsapp"){
            if (sbn.tag !=null){
                onNewNotification(notificationData)
            }

        } else {
            Log.d("Not Message Notification", "Not Whatsapp Notification")
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("Notification On", "Notification Connected")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.i("Removed Notification", "Notifikasi telah dihapus")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("NotificationService", "Service started")
        return START_REDELIVER_INTENT
    }

    private fun onNewNotification(notificationData: NotificationItemModel) {
        Log.i("NotificationService", "New Notification has been posted: $notificationData")
        val intent = Intent().apply {
            action = ACTION_NEW_NOTIFICATION
            putExtra("notification_data", notificationData)
        }
        sendBroadcast(intent)
    }



    companion object {
        const val ACTION_NEW_NOTIFICATION = "com.skripsi.dosa.NEW_NOTIFICATION"
    }


}