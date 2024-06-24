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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.skripsi.dosa.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var notificationViewModel: NotificationViewModel

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize ViewModel
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        navController.navigate(R.id.safeMessageFragment)

        askNotificationPermission()

        // Use coroutine to add a delay before checking LISTENER_CONNECTED
        CoroutineScope(Dispatchers.Main).launch {
            delay(10000)
            checkNotificationListenerConnected()
        }

        binding.menuNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
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
                else -> false
            }
        }
    }

    private fun checkNotificationListenerConnected() {
        val notificationListenerSetting = ComponentName(this, NotificationService::class.java)
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val hasNotificationAccess =
            enabledListeners?.contains(notificationListenerSetting.flattenToString()) ?: false

        if (!hasNotificationAccess) {
            Toast.makeText(
                this,
                "Please Grant the Permission to use notification",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        } else if (!NotificationService.LISTENER_CONNECTED) {
            Toast.makeText(
                this,
                "Notification listener is not connected. Please check your settings.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
