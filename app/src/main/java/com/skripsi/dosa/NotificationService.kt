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
import java.text.SimpleDateFormat
import java.util.Date


class NotificationService: NotificationListenerService() {

    private var serviceStartTime: Long = 0
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SimpleDateFormat")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val extras = sbn?.notification?.extras

        val id = sbn?.id?.toLong()
        val tag = sbn?.tag.toString()
        val title = extras?.getString("android.title") ?:"test"
        val text = extras?.getCharSequence("android.text").toString()
        val postedTime = sbn?.postTime ?: 0
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(postedTime))
        val packageName = sbn?.packageName
        val notificationAge = System.currentTimeMillis() - postedTime
        val notificationData =
            id?.let { NotificationItemModel(it, tag, title, text, date.toString(), packageName) }
        val registerEx = Regex(util.regstr)

        if(sbn!!.packageName == "com.whatsapp.w4b" || sbn.packageName == "com.whatsapp"){
            if (notificationAge > serviceStartTime){
                if (registerEx.matches(title)){
                    if (sbn.tag !=null){
                        val ortEnvironment = OrtEnvironment.getEnvironment()
                        val ortSession = createORTSession( ortEnvironment )
                        val output = runPrediction(text, ortSession, ortEnvironment)
                        if (output == "Not Spam"){
                           if (notificationData != null) {
                                onNewNotification(notificationData, "not_spam")
                            }
                        } else if(output == "Spam") {
                            Log.e("Spam Message", "This is spam and phising")
                            if (notificationData != null) {
                                onNewNotification(notificationData, "spam")
                            }
                            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                                .setContentTitle("Dangerous Message !! Please be aware !!!")
                                .setContentText(text)
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
                    }
                } else if (notificationData != null) {
                        onNewNotification(notificationData, "not_spam")
                }
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
        serviceStartTime = System.currentTimeMillis()
        return START_REDELIVER_INTENT
    }

    private fun onNewNotification(notificationData: NotificationItemModel, check: String ) {
        Log.i("NotificationService", "New Notification has been posted: $notificationData")
        if(check == "spam"){
            val intent = Intent(ACTION_NEW_DANGEROUS_NOTIFICATION).apply {
                putExtra("notification_spam_data", notificationData)
                Log.i("intent_spam_from_notification_service", notificationData.toString())
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } else if(check == "not_spam"){
            val intent = Intent(ACTION_NEW_NOTIFICATION).apply {
                putExtra("notification_data", notificationData)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

    }

    companion object {
        const val ACTION_NEW_NOTIFICATION = "com.skripsi.dosa.NEW_NOTIFICATION"
        const val ACTION_NEW_DANGEROUS_NOTIFICATION = "com.skripsi.dosa.NEW_DANGEROUS_NOTIFICATION"
        const val CHANNEL_ID = "MY_CHANNEL_ID"
    }

    private fun createORTSession( ortEnvironment: OrtEnvironment) : OrtSession {
        val modelBytes = resources.openRawResource( R.raw.spam ).readBytes()
        return ortEnvironment.createSession( modelBytes )
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
        val output = results[0].value as LongArray // Assuming output is LongArray
        // Convert LongArray to FloatArray
        val floatOutput = FloatArray(output.size) { output[it].toFloat() }
        return if (floatOutput[0] == 1f) "Spam" else "Not Spam"
    }

}