package com.delivery.setting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.setting.databinding.ItemOrderHistoryBinding
import com.delivery.setting.databinding.ItemReorderBinding
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus

class OrderHistoryPagingAdapter(
    private val onItemClick: (OrderHistoryItem) -> Unit
) : PagingDataAdapter<OrderHistoryItem, OrderHistoryPagingAdapter.OrderHistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    inner class OrderHistoryViewHolder(
        private val binding: ItemOrderHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val item = getItem(adapterPosition)
                item?.let { onItemClick(it) }
            }
        }

        fun bind(item: OrderHistoryItem) {
            binding.apply {
                tvFromLocation.text = item.fromLocation
                tvFromAddress.text = item.fromAddress
                tvToLocation.text = item.toLocation
                tvToAddress.text = item.toAddress
                tvPackageType.text = item.packageType
                tvPackageWeight.text = item.packageWeight
                tvOrderStatus.text = getStatusText(item.status)
                tvOderDate.text = formatOrderDate(item.orderDate, item.orderTime)
                
                // Set status color based on order status
                val statusColor = when (item.status) {
                    OrderStatus.DELIVERED -> android.R.color.holo_green_dark
                    OrderStatus.IN_PROGRESS -> android.R.color.holo_orange_dark
                    OrderStatus.PENDING -> android.R.color.holo_blue_dark
                    OrderStatus.CONFIRMED -> android.R.color.holo_purple
                    OrderStatus.CANCELLED -> android.R.color.holo_red_dark
                }
                tvOrderStatus.setTextColor(binding.root.context.getColor(statusColor))
            }
        }

        private fun getStatusText(status: OrderStatus): String {
            return status.displayName
        }

        private fun formatOrderDate(date: String, time: String?): String {
            return if (time != null) {
                "$date • $time"
            } else {
                date
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<OrderHistoryItem>() {
            override fun areItemsTheSame(oldItem: OrderHistoryItem, newItem: OrderHistoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OrderHistoryItem, newItem: OrderHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
