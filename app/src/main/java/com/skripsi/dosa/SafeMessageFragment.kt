package com.skripsi.dosa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        registerReceiver()

        notificationViewModel.safeMessageItem.observe(viewLifecycleOwner, Observer { notifications ->
            notifications?.let {
                adapter.updateData(it)
                Log.d("Info data", notifications.toString())
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
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NotificationService.ACTION_NEW_NOTIFICATION) {
                    val notificationData = intent.getParcelableExtra<NotificationItemModel>("notification_data")
                    notificationData?.let {
                        notificationViewModel.addNotification(it)
                        Log.d("SafeMessageFragment", "Received notification: $notificationData")
                    }
                }
            }
        }
        val filter = IntentFilter(NotificationService.ACTION_NEW_NOTIFICATION)
        requireContext().let {
            LocalBroadcastManager.getInstance(it).registerReceiver(broadcastReceiver, filter)
        }
    }

    private fun unregisterReceiver() {
        requireContext().let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver)
        }
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter(mutableListOf())
        binding.recyclerViewItem.adapter = adapter
        binding.recyclerViewItem.layoutManager = LinearLayoutManager(context)
    }
}