package com.skripsi.dosa

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.time.LocalTime


class NotificationService : NotificationListenerService() {

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SimpleDateFormat")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val extras = sbn?.notification?.extras
        val tag = sbn?.tag.toString()
        val title = extras?.getCharSequence("android.title")
        val text: CharSequence? = extras?.getCharSequence("android.text")
        val processedText = processContent(text.toString())
        val time = LocalTime.now()
        val packageName = sbn?.packageName
        val notificationData =
            NotificationItemModel(
                id = 0,
                tag,
                title.toString(),
                text.toString(),
                time.toString(),
                packageName,
            )
        val registerEx = Regex(Regstr)

        if (sbn!!.packageName == "com.whatsapp.w4b" || sbn.packageName == "com.whatsapp") {
            if (sbn.tag != null) {
                if (notificationData.title.let { registerEx.matches(it.toString()) }) {
                    val ortEnvironment = OrtEnvironment.getEnvironment()
                    val ortSession = createORTSession(ortEnvironment)
                    val output = runPrediction(processedText, ortSession, ortEnvironment)
                    if (output == "Not Spam") {
                        onNewNotification(notificationData)
                    } else if (output == "Spam") {
                        onSpamNewNotification(notificationData)
                        postNotification(notificationData.text.toString(), notificationData.title.toString(), messageCount )
                        messageCount++
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
    }

    private fun onNewNotification(notificationData: NotificationItemModel) {
        val intent = Intent(ACTION_NEW_NOTIFICATION).apply {
            putExtra("notification_data", notificationData)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun onSpamNewNotification(notificationData: NotificationItemModel) {
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

    private fun postNotification(message: String, sender:String, messageCount : Int) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Dangerous Message from $sender !! Please be aware !!!")
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
        mNotificationManager.notify(messageCount, builder.build())
    }

    private fun processContent(content: String): String {
        var processedContent = content.trim()

        // Remove URLs
        val urlPattern = Regex("https?://\\S+|www\\.\\S+")
        processedContent = urlPattern.replace(processedContent, "")

        // Remove HTML tags
        val htmlPattern = Regex("<.*?>")
        processedContent = htmlPattern.replace(processedContent, "")

        // Remove numbers
        val numberPattern = Regex("[-+]?[0-9]+")
        processedContent = numberPattern.replace(processedContent, "")

        // Remove punctuation
        val punctuationPattern = Regex("[^\\w\\s]")
        processedContent = punctuationPattern.replace(processedContent, "")

        // Convert to lowercase
        processedContent = processedContent.lowercase()

        return processedContent
    }


    companion object {
        const val ACTION_NEW_NOTIFICATION = "com.skripsi.dosa.NEW_NOTIFICATION"
        const val ACTION_NEW_DANGEROUS_NOTIFICATION = "com.skripsi.dosa.NEW_DANGEROUS_NOTIFICATION"
        const val CHANNEL_ID = "MY_CHANNEL_ID"
        var LISTENER_CONNECTED = false
        var messageCount = 0
        const val Regstr = "(\\+\\d{1,3}( )?)?([-( ]\\d{3,5}[-) ])( )?\\d{3,5}-\\d{3,4}"
    }
}