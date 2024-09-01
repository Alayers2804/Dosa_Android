package com.skripsi.dosa


import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.skripsi.dosa.databinding.ActivityMainBinding
import java.util.Timer
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var notificationViewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotificationPermission()
        checkNotificationListenerConnected(true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        notificationViewModel =
            ViewModelProvider(this)[NotificationViewModel::class.java] // Initialize ViewModel here

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.safeMessageFragment -> {
                    binding.menuNavigation.menu.findItem(R.id.action_safe_message).isChecked = true
                    binding.menuNavigation.visibility = View.VISIBLE
                }

                R.id.spamMessageFragment -> {
                    binding.menuNavigation.menu.findItem(R.id.action_dangerous_message).isChecked =
                        true
                    binding.menuNavigation.visibility = View.VISIBLE
                }

                else -> {
                    binding.menuNavigation.visibility = View.VISIBLE
                }
            }
        }

        binding.menuNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_safe_message -> {
                    if (navController.currentDestination?.id != R.id.safeMessageFragment) {
                        navController.navigate(R.id.safeMessageFragment)
                    }
                    true
                }

                R.id.action_dangerous_message -> {
                    navController.navigate(R.id.spamMessageFragment)
                    true
                }

                R.id.action_restart -> {
                    checkNotificationListenerConnected(false)
                    true
                }

                else -> false
            }
        }
        registerReceiver()

    }

    private fun askNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return false
            }
        }
        return true
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "FCM can't post notifications without POST_NOTIFICATIONS permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun checkNotificationListenerConnected(useTimer: Boolean) {
        val notificationListenerSetting = ComponentName(this, NotificationService::class.java)
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val hasNotificationAccess =
            enabledListeners?.contains(notificationListenerSetting.flattenToString()) ?: false

        val checkAndNotify = {
            if (!hasNotificationAccess) {
                Toast.makeText(
                    this@MainActivity,
                    "Please Grant the Permission to use notification",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } else if (!NotificationService.LISTENER_CONNECTED) {
                Toast.makeText(
                    this@MainActivity,
                    "Notification listener is not connected. Please check your settings.",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
        if (useTimer) {
            Timer().schedule(timerTask {
                runOnUiThread(checkAndNotify)
            }, 10000)
        } else {
            checkAndNotify()
        }
    }


    fun showBottomBar() {
        binding.menuNavigation.visibility = View.VISIBLE
    }

    fun hideBottomBar() {
        binding.menuNavigation.visibility = View.GONE
    }

    private fun registerReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NotificationService.ACTION_NEW_NOTIFICATION) {
                    val notificationData =
                        intent.getParcelableExtra<NotificationItemModel>("notification_data")
                    notificationData?.let {
                        notificationViewModel.addSafeNotification(it)
                    }
                } else if (intent?.action == NotificationService.ACTION_NEW_DANGEROUS_NOTIFICATION) {
                    val notificationData =
                        intent.getParcelableExtra<NotificationItemModel>("spam_notification_Data")
                    notificationData?.let {
                        notificationViewModel.updateSpamNotifications(it)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(NotificationService.ACTION_NEW_NOTIFICATION)
            addAction(NotificationService.ACTION_NEW_DANGEROUS_NOTIFICATION)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)
    }

//    fun observeViewModel() {
//        notificationViewModel.safeMessageItem.observe(this) { notifications ->
//            notifications?.let {
//                Log.i("MainActivity", "Updated data safe message fragment: $it")
//
//            }
//        }
//    }

//    fun observeDangerousViewModel() {
//        notificationViewModel.spamNotifications.observe(this) { notifications ->
//            notifications?.let {
//                Log.i("MainActivity", "Updated data safe message fragment: $it")
//
//            }
//        }
//    }

}

