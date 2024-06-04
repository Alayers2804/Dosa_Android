package com.skripsi.dosa


import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.skripsi.dosa.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ItemAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { NotificationRoomDatabase.getDatabase(this, applicationScope) }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        askNotificationPermission()

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


//        //To register NotificationService Service to main activity
//        val filter = IntentFilter().apply {
//            addAction(ACTION_NEW_NOTIFICATION)
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED)
//        }

        binding.menuNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.action_safe_message -> {
                    // Respond to navigation item 1 click
                    if (navController.currentDestination?.id != R.id.safeMessageFragment) {
                        navController.navigate(R.id.safeMessageFragment)
                    }
                    true
                }
                R.id.action_dangerous_message -> {
                    // Respond to navigation item 2 click
                     navController.navigate(R.id.spamMessageFragment)
                    true
                }
                else -> false
            }
        }
    }

    //Function for verifying every action received from the Notification Service and add it to the UI in Main Activiy
//    private val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == ACTION_NEW_NOTIFICATION) {
//                val notificationData =
//                    intent.getParcelableExtra<NotificationItemModel>("notification_data")
//                notificationData?.let {
//                    adapter.addData(notificationData)
//                }
//            }
//        }
//    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this,
                "FCM can't post notifications without POST_NOTIFICATIONS permission",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

