package com.skripsi.dosa

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ItemAdapter(private var dataList: List<NotificationItemModel>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.userView.text = item.title.toString()
        holder.contentView.text = item.text.toString()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userView: TextView = itemView.findViewById(R.id.txt_userName)
        val contentView: TextView = itemView.findViewById(R.id.txt_Content)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(notificationData: NotificationItemModel) {
        dataList = dataList.plus(notificationData) // Add the new notification data to the existing list
        notifyDataSetChanged() // Notify the RecyclerView that data has changed
    }
}