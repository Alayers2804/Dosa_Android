package com.skripsi.dosa


import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skripsi.dosa.NotificationService.Companion.ACTION_NEW_NOTIFICATION
import com.skripsi.dosa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ItemAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val notificationListenerSetting = ComponentName(this, NotificationService::class.java)
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val hasNotificationAccess =
            enabledListeners?.contains(notificationListenerSetting.flattenToString()) ?: false

        if (!hasNotificationAccess) {
            // Permission not granted, launch notification listener settings
            Toast.makeText(
                this,
                "Please Grant the Permission to use notification",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }


        //To register NotificationService Service to main activity
        val filter = IntentFilter().apply {
            addAction(ACTION_NEW_NOTIFICATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED)
        }

        //Binding RecyclerView to ItemAdapter Item-list.xml
        val recyclerView = binding.itemList
        adapter = ItemAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

    }

    //Function for verifying every action received from the Notification Service and add it to the UI in Main Activiy
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_NEW_NOTIFICATION) {
                val notificationData =
                    intent.getParcelableExtra<NotificationItemModel>("notification_data")
                notificationData?.let {
                    Log.i("MainActivity", "Received new notification: $notificationData")
                    adapter.addData(notificationData)
                }
            }
        }
    }
}

