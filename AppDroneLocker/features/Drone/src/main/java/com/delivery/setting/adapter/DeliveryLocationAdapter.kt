package com.delivery.setting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delivery.setting.databinding.ItemDeliveryLocationBinding
import com.delivery.setting.model.DeliveryLocation

class DeliveryLocationAdapter(
    private val onItemClick: (DeliveryLocation) -> Unit
) : ListAdapter<DeliveryLocation, DeliveryLocationAdapter.DeliveryLocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryLocationViewHolder {
        val binding = ItemDeliveryLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeliveryLocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeliveryLocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeliveryLocationViewHolder(
        private val binding: ItemDeliveryLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(item: DeliveryLocation) {
            binding.apply {
                tvLocationName.text = item.name
                tvLocationAddress.text = item.address
                tvDeliveryTime.text = item.lastDeliveryTime ?: "Chưa từng giao"
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DeliveryLocation>() {
            override fun areItemsTheSame(oldItem: DeliveryLocation, newItem: DeliveryLocation): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DeliveryLocation, newItem: DeliveryLocation): Boolean {
                return oldItem == newItem
            }
        }
    }
}
