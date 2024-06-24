package com.skripsi.dosa

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.DelicateCoroutinesApi
import java.net.IDN
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date


class NotificationService : NotificationListenerService() {

    @SuppressLint("SimpleDateFormat")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val extras = sbn?.notification?.extras
        val id = notificationIdCounter++
        val tag = sbn?.tag.toString()
        val title = extras?.getString("android.title") ?: "test"
        val text = extras?.getCharSequence("android.text").toString()
        val time = LocalTime.now()
        val packageName = sbn?.packageName
        val notificationData =
            NotificationItemModel(
                id,
                tag,
                title,
                text,
                time.toString(),
                packageName,
                spam = false
            )
        val registerEx = Regex(util.regstr)
//        val postedTime = sbn?.postTime ?: 0
//        val notificationAge = System.currentTimeMillis() - postedTime
        if (sbn!!.packageName == "com.whatsapp.w4b" || sbn.packageName == "com.whatsapp") {
            if (sbn.tag != null) {
                if (notificationData.title.let { registerEx.matches(it) }) {
                    val ortEnvironment = OrtEnvironment.getEnvironment()
                    val ortSession = createORTSession(ortEnvironment)
                    val output = runPrediction(notificationData.text!!, ortSession, ortEnvironment)
                    if (output == "Not Spam") {
                        onNewNotification(notificationData)
                    } else if (output == "Spam") {
                        Log.e("Spam Message", "This is spam and phising")
                        notificationData.spam = true
                        onSpamNewNotification(notificationData)
                        postNotification(notificationData.text)
                    }

                } else {
                    onNewNotification(notificationData)
                }
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        LISTENER_CONNECTED = true
        Log.i("Notification On", "Notification Connected")
    }

    private fun onNewNotification(notificationData: NotificationItemModel) {
        Log.i("NotificationService", "New Notification has been posted: $notificationData")
        val intent = Intent(ACTION_NEW_NOTIFICATION).apply {
            putExtra("notification_data", notificationData)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun onSpamNewNotification(notificationData: NotificationItemModel){
        val intent = Intent(ACTION_NEW_DANGEROUS_NOTIFICATION).apply {
            putExtra("spam_notification_Data", notificationData)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    private fun createORTSession(ortEnvironment: OrtEnvironment): OrtSession {
        val modelBytes = resources.openRawResource(R.raw.spam).readBytes()
        return ortEnvironment.createSession(modelBytes)
    }

    private fun runPrediction(
        input: String,
        ortSession: OrtSession,
        ortEnvironment: OrtEnvironment
    ): String {
        // Get the name of the input node
        val inputName = ortSession.inputNames?.iterator()?.next()
        val inputTensor = OnnxTensor.createTensor(ortEnvironment, arrayOf(input), longArrayOf(1))
        val results = ortSession.run(mapOf(inputName to inputTensor))
        val output = results[0].value as LongArray
        val floatOutput = FloatArray(output.size) { output[it].toFloat() }
        return if (floatOutput[0] == 1f) "Spam" else "Not Spam"
    }

    private fun postNotification(message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Dangerous Message !! Please be aware !!!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
        val mNotificationManager: NotificationManager = this.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val channelId = "Your_channel_id"
        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_HIGH
        )
        mNotificationManager.createNotificationChannel(channel)
        builder.setChannelId(channelId)
        mNotificationManager.notify(0, builder.build())
    }

    companion object {
        const val ACTION_NEW_NOTIFICATION = "com.skripsi.dosa.NEW_NOTIFICATION"
        const val ACTION_NEW_DANGEROUS_NOTIFICATION = "com.skripsi.dosa.NEW_DANGEROUS_NOTIFICATION"
        const val CHANNEL_ID = "MY_CHANNEL_ID"
        var LISTENER_CONNECTED = false
        var notificationIdCounter: Long = 0
    }
}