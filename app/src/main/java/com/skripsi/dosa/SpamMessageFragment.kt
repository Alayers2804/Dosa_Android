package com.skripsi.dosa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.skripsi.dosa.databinding.FragmentSpamMessageBinding


class SpamMessageFragment : Fragment() {

    private var _binding : FragmentSpamMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ItemAdapter
    private lateinit var broadcastReceiver: BroadcastReceiver
    private val notificationViewModel: NotificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpamMessageBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        registerReceiver()

        notificationViewModel.safeMessageItem.observe(viewLifecycleOwner, Observer { notifications ->
            adapter.updateData(notifications)
        })

    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        unregisterReceiver()
//    }

    private fun setupRecyclerView(){
        adapter = ItemAdapter(mutableListOf())
        binding.recyclerViewSpamItem.adapter = adapter
        binding.recyclerViewSpamItem.layoutManager = LinearLayoutManager(context)
    }

    private fun registerReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NotificationService.ACTION_NEW_NOTIFICATION) {
                    val notificationData =
                        intent.getParcelableExtra<NotificationItemModel>("notification_data")
                    notificationData?.let {
                        adapter.addData(notificationData)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(NotificationService.ACTION_NEW_NOTIFICATION)
        }
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(broadcastReceiver, filter)
        }
    }

    private fun unregisterReceiver() {
        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver)
        }
    }

}