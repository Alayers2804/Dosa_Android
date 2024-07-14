package com.skripsi.dosa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skripsi.dosa.databinding.FragmentDetailBinding


class DetailMessageFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ItemAdapter


    private val notificationViewModel: NotificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        adapter = ItemAdapter(mutableListOf()) {   // make sure to handle item clicks if necessary
        }
        (activity as? MainActivity)?.hideBottomBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tag = arguments?.getString("tag")
        if (tag != null) {
            notificationViewModel.fetchNotificationsByTag(tag)
        }

        setupRecyclerView()
        setupObservers()

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp() // Navigates back to the previous fragment
        }

    }

    private fun setupObservers() {
        val tag = arguments?.getString("tag")
        tag?.let {
            notificationViewModel.fetchNotificationsByTag(it)
            notificationViewModel.safeMessagePerTagItem.observe(
                viewLifecycleOwner,
                Observer { notifications ->
                    notifications?.let { items ->
                        adapter.updateData(items)
                    }
                })
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewDetail.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewDetail.adapter = adapter
    }
}

