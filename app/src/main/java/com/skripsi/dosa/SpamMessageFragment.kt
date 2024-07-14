package com.skripsi.dosa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.skripsi.dosa.databinding.FragmentSpamMessageBinding


class SpamMessageFragment : Fragment() {

    private var _binding: FragmentSpamMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ItemAdapter

    //    private lateinit var broadcastReceiver: BroadcastReceiver
    private val notificationViewModel: NotificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpamMessageBinding.inflate(inflater, container, false)
        val view = binding.root
        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        registerReceiver()

        notificationViewModel.spamNotifications.observe(
            viewLifecycleOwner,
            Observer { notifications ->
                notifications?.let {
                    adapter.updateData(it)
                    Log.d("Observer_item", it.toString())
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        unregisterReceiver()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter(mutableListOf()) {
            println("Do not click")
        }
        binding.recyclerViewSpamItem.adapter = adapter
        binding.recyclerViewSpamItem.layoutManager = LinearLayoutManager(context)
    }

//    private fun registerReceiver() {
//        broadcastReceiver = object : BroadcastReceiver() {
//            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action == NotificationService.ACTION_NEW_DANGEROUS_NOTIFICATION) {
//                    val notificationData = intent.getParcelableExtra("spam_notification_Data", NotificationItemModel::class.java)
//                    notificationData?.let {
//                        notificationViewModel.updateSpamNotifications(it)
//                        Log.d("SpamMessageFragment", "Received notification: $notificationData")
//                    }
//                }
//            }
//        }
//        val filter = IntentFilter(NotificationService.ACTION_NEW_DANGEROUS_NOTIFICATION)
//        context?.let {
//            LocalBroadcastManager.getInstance(it).registerReceiver(broadcastReceiver, filter)
//        }
//    }


//    private fun unregisterReceiver() {
//        context?.let {
//            LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver)
//        }
//    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomBar()
    }
}
