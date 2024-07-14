package com.skripsi.dosa

import android.content.BroadcastReceiver
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skripsi.dosa.databinding.FragmentSafeMessageBinding

class SafeMessageFragment : Fragment() {

    private var _binding: FragmentSafeMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ItemAdapter

    private val notificationViewModel: NotificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafeMessageBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        registerReceiver()
//        refreshNotifications()

        notificationViewModel.safeMessageItem.observe(
            viewLifecycleOwner
        ) { notifications ->
            notifications?.let {
                adapter.updateData(it)
                Log.i("Updated data safe message fragment", it.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        unregisterReceiver()
        _binding = null
    }


    private fun setupRecyclerView() {
        adapter = ItemAdapter(mutableListOf()) { tag ->
            showDetailFragment(tag)
        }
        binding.recyclerViewItem.adapter = adapter
        binding.recyclerViewItem.layoutManager = LinearLayoutManager(context)

    }


    private fun showDetailFragment(tag: String) {
        val bundle = Bundle()
        bundle.putString("tag", tag)
        findNavController().navigate(
            R.id.action_safeMessageFragment_to_detailMessageFragment,
            bundle
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomBar()
    }

}
