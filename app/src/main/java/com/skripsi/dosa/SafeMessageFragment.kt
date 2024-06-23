package com.skripsi.dosa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.skripsi.dosa.databinding.FragmentSafeMessageBinding

class SafeMessageFragment : Fragment() {

    private var _binding: FragmentSafeMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ItemAdapter

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val notificationViewModel: NotificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafeMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        registerReceiver()

        refreshNotifications()
        // Observe the LiveData from ViewModel
        notificationViewModel.safeMessageItem.observe(viewLifecycleOwner, Observer { notifications ->
            notifications?.let {
                adapter.updateData(it)
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterReceiver()
        _binding = null
    }

    private fun registerReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("SafeMessageFragment", "Broadcast received: ${intent?.action}")
                if (intent?.action == NotificationService.ACTION_NEW_NOTIFICATION) {
                    val notificationData = intent.getParcelableExtra("notification_data", NotificationItemModel::class.java)
                    notificationData?.let {
                        notificationViewModel.addSafeNotification(it)
                        Log.d("SafeMessageFragment", "Received notification: $notificationData")
                    }
                }
            }
        }
        val filter = IntentFilter(NotificationService.ACTION_NEW_NOTIFICATION)
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(broadcastReceiver, filter)
        }
    }

    private fun unregisterReceiver() {
        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver)
        }
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter(mutableListOf())
        binding.recyclerViewItem.adapter = adapter
        binding.recyclerViewItem.layoutManager = LinearLayoutManager(context)
    }

    fun refreshNotifications() {
        // This will trigger the ViewModel to fetch the latest notifications from the database
        notificationViewModel.fetchAllSafeNotifications()
    }
}
