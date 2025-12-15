package com.delivery.setting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delivery.core.utils.getDimension
import com.delivery.setting.R
import com.delivery.setting.databinding.ItemReorderBinding
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus

class ReorderAdapter(
    private val onItemClick: (OrderHistoryItem) -> Unit,
) : ListAdapter<OrderHistoryItem, ReorderAdapter.ReorderViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ReorderViewHolder {
        val binding =
            ItemReorderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ReorderViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ReorderViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class ReorderViewHolder(
        private val binding: ItemReorderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(item: OrderHistoryItem) {
            binding.apply {
                tvFromLocation.text = item.sourceLocker
                tvFromAddress.text = "" // Không cần hiển thị địa chỉ nữa vì đã có tên locker
                tvToLocation.text = item.destLocker
                tvToAddress.text = "" // Không cần hiển thị địa chỉ nữa vì đã có tên locker
                tvPackageType.text = "Loại hàng" //orderItem.packageType
                tvPackageWeight.text = item.packageWeight
                tvOrderStatus.text = getStatusText(item.status)
                tvOderDate.text = formatOrderDate(item.orderDate, item.orderTime)

                // Set status color based on order status
                val statusColor =
                    when (item.status) {
                        OrderStatus.PENDING -> android.R.color.holo_blue_dark
                        OrderStatus.CONFIRMED -> android.R.color.holo_orange_light
                        OrderStatus.IN_DELIVERY -> android.R.color.holo_orange_dark
                        OrderStatus.DELIVERED -> android.R.color.holo_green_dark
                        OrderStatus.CANCEL -> android.R.color.holo_red_dark
                    }
                tvOrderStatus.setTextColor(binding.root.context.getColor(statusColor))
            }
            val layoutParams =
                binding.clContainer.layoutParams as ViewGroup.MarginLayoutParams
            if (itemCount == 1) {
                layoutParams.setMargins(
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_8).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                )
            } else {
                val marginStart =
                    if (position == 0) {
                        binding.clContainer.context.getDimension(R.dimen.dimen_16)
                    } else {
                        binding.clContainer.context.getDimension(R.dimen.dimen_8)
                    }
                val marginEnd =
                    if (position == itemCount - 1) {
                        binding.clContainer.context.getDimension(R.dimen.dimen_16)
                    } else {
                        binding.clContainer.context.getDimension(R.dimen.dimen_8)
                    }

                layoutParams.setMargins(
                    marginStart.toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_8).toInt(),
                    marginEnd.toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                )
            }
        }

        private fun getStatusText(status: OrderStatus): String {
            return status.displayName
        }

        private fun formatOrderDate(
            date: String,
            time: String?,
        ): String {
            return if (time != null) {
                "$date • $time"
            } else {
                date
            }
        }
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<OrderHistoryItem>() {
                override fun areItemsTheSame(
                    oldItem: OrderHistoryItem,
                    newItem: OrderHistoryItem,
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: OrderHistoryItem,
                    newItem: OrderHistoryItem,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
